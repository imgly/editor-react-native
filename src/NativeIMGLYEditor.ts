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
  /** The license of the editor. */
  license: string;

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
  /** The license of the editor. */
  license: string;

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

    const modifiedSource =
      source?.source != null
        ? {
            ...source,
            source: resolveStaticAsset(source.source)
          }
        : source;

    return NativeModule.openEditor(settings, modifiedSource, preset, metadata);
  }
}

export default IMGLYEditor;
