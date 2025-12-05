export type compressionMethod = 'auto' | 'manual';
export type videoCodec = 'h264' | 'hevc';
type videoCompresssionType = {
    bitrate?: number;
    maxSize?: number;
    compressionMethod?: compressionMethod;
    minimumFileSizeForCompress?: number;
    getCancellationId?: (cancellationId: string) => void;
    downloadProgress?: (progress: number) => void;
    /***
     * Default:0, we uses it when we use downloadProgress/onProgress
     */
    progressDivider?: number;
    /***
     * Video codec to use for compression.
     * 'h264' - H.264/AVC codec (default, widely compatible)
     * 'hevc' - H.265/HEVC codec (better compression, iOS 11+/Android 5+)
     */
    videoCodec?: videoCodec;
};
export type VideoCompressorType = {
    compress(fileUrl: string, options?: videoCompresssionType, onProgress?: (progress: number) => void): Promise<string>;
    cancelCompression(cancellationId: string): void;
    activateBackgroundTask(onExpired?: (data: any) => void): Promise<any>;
    deactivateBackgroundTask(): Promise<any>;
    /**
     * Check if the device supports hardware HEVC/H.265 encoding.
     * @returns Promise<boolean> - true if HEVC hardware encoding is supported
     */
    isHEVCEncoderSupported(): Promise<boolean>;
    /**
     * Get the optimal video codec for compression based on hardware support.
     * Returns 'hevc' if hardware HEVC encoding is supported, otherwise 'h264'.
     * @returns Promise<videoCodec> - 'hevc' or 'h264'
     */
    getOptimalCodec(): Promise<videoCodec>;
};
export declare const cancelCompression: (cancellationId: string) => any;
declare const Video: VideoCompressorType;
export default Video;
