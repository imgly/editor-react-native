import { Image, TurboModule, TurboModuleRegistry } from 'react-native';

/** An editor preset for an existing UI configuration. */
export enum EditorPreset {
  /** Design editor. */
  DESIGN = 'design',

  /** Photo editor. */
  PHOTO = 'photo',

  /** Postcard editor. */
  POSTCARD = 'postcard',

  /** Apparel editor. */
  APPAREL = 'apparel',

  /** Video editor. */
  VIDEO = 'video'
}

/**
 * An editor result is returned from a successful
 * editor export and contains the relevant export
 * information.
 */
export interface EditorResult {
  /** The scene.
   * ⚠️ On Android, this scene might contain blocks that reference
   * `content://` Uri(s) (e.g. when media is picked from gallery or camera).
   * These may not be resolvable when loading the scene again, especially
   * outside the original app context.
   *
   * If you need to support reloading such scenes, consider using a custom
   * implementation with `EngineConfiguration.onUpload` and
   * `engine.editor.setUriResolver` to handle these Uri(s) appropriately.
   */
  scene?: string;

  /** The path of the exported image/video/pdf. */
  artifact?: string;

  /** The path of the thumbnail of the artifact. */
  thumbnail?: string;

  /**
   * Metadata associated with the export.
   * Should be customizable by the customer
   * using the onExport interface.
   */
  metadata: { [key: string]: unknown };
}

/**
 * The EditorSettings are used to provide the
 * information needed by the editor to operate.
 */
export interface EditorSettings {
  /** The license of the editor. Pass `null` to run the SDK in evaluation mode with a watermark. */
  license?: string;

  /**
   * The base URI to the assets included in
   * the scene of which the sources are specified
   * as relative paths.
   */
  sceneBaseUri: string;

  /** The base URI to the default assets of the asset library. */
  assetBaseUri?: string;

  /**
   * Unique ID tied to your application's user.
   * This helps us accurately calculate monthly
   * active users (MAU).
   */
  userId?: string;
}

/**
 * Default implementation of the `EditorSettings`.
 *
 * The EditorSettings are used to provide the
 * information needed by the editor to operate.
 */
export class EditorSettingsModel implements EditorSettings {
  /** The license of the editor. Pass `null` to run the SDK in evaluation mode with a watermark. */
  license?: string;

  /**
   * The base URI to the assets included in
   * the scene of which the sources are specified
   * as relative paths.
   */
  sceneBaseUri: string =
    'https://cdn.img.ly/packages/imgly/cesdk-engine/1.51.0/assets';

  /** The base URI to the default assets of the asset library. */
  assetBaseUri?: string;

  /**
   * Unique ID tied to your application's user.
   * This helps us accurately calculate monthly
   * active users (MAU).
   */
  userId?: string;

  constructor(settings: Partial<EditorSettings> = {}) {
    Object.assign(this, settings);
  }
}

/**
 * Defines all available types of source that can be
 * loaded into the editor.
 */
export enum SourceType {
  /** A `.scene` file. */
  SCENE = 'scene',

  /** An image, e.g. `.png`. */
  IMAGE = 'image',

  /** A video, e.g. `.mp4`. */
  VIDEO = 'video'
}

/** A source for the editor. */
export interface Source {
  /**
   * The source location.
   * The source should be pointing to a valid file based
   * on the selected `type`.
   */
  source: string;

  /** The type of the source. */
  type: SourceType;
}

/** TurboModule Spec. */
interface Spec extends TurboModule {
  /**
   * Open the Creative Editor.
   * @param settings The `EditorSettings`.
   * @param source The `Source` to open.
   * @param preset The editor variant to open.
   * @param metadata The metadata to pass to the native module.
   */
  openEditor(
    settings: EditorSettings,
    source?: Source,
    preset?: EditorPreset,
    metadata?: { [key: string]: unknown }
  ): Promise<EditorResult | null>;
}

/** The native module. */
const NativeModule = TurboModuleRegistry.get<Spec>(
  'IMGLYEditor'
) as Spec | null;

/**
 * Checks if the app is using the New Architecture (TurboModules or Bridgeless).
 * @returns true if New Architecture is enabled, false for Bridge.
 */
function isNewArchitectureEnabled(): boolean {
  const g = global as any;
  // Check for TurboModules (Expo 51-52 / RN 0.74-0.76)
  // Check for Bridgeless mode (Expo 53+ / RN 0.79+)
  return g.__turboModuleProxy != null || g.RN$Bridgeless === true;
}

/**
 * Resolves assets that are imported via `require`.
 * @param assetSource The asset source, e.g. require('my-asset')
 * @returns The resolved URI.
 */
function resolveStaticAsset(assetSource: any): string {
  const resolvedSource = Image.resolveAssetSource(assetSource);
  if (resolvedSource) {
    return resolvedSource.uri;
  }
  return assetSource;
}

/** The IMG.LY Creative Editor. */
class IMGLYEditor {
  /**
   * Open the Creative Editor.
   * @param settings The `EditorSettings`.
   * @param source The `Source` to open.
   * @param preset The editor variant to open.
   * @param metadata The metadata to pass to the native module.
   */
  static async openEditor(
    settings: EditorSettings,
    source?: Source,
    preset?: EditorPreset,
    metadata?: { [key: string]: unknown }
  ): Promise<EditorResult | null> {
    if (NativeModule == null) return null;

    let modifiedSource: Source | undefined;
    if (source?.source != null) {
      modifiedSource = {
        ...source,
        source: resolveStaticAsset(source.source)
      };
    } else if (isNewArchitectureEnabled()) {
      // iOS New Architecture: Pass empty object to ensure struct conversion is triggered.
      // Passing undefined leaves the C++ struct with garbage memory in release builds.
      // See: https://github.com/facebook/react-native/issues/49920
      modifiedSource = {} as unknown as Source;
    }
    // iOS Bridge (Old Architecture): Pass undefined, which the bridge handles
    // gracefully. Passing {} causes decoding errors.

    return NativeModule.openEditor(
      settings,
      modifiedSource,
      preset,
      metadata
    );
  }
}

export default IMGLYEditor;
