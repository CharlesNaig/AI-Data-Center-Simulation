# V2.2 artwork

Seven original transparent PNG sprite assets were generated for this model with Codex's **built-in image-generation tool** on 2026-10-13. They are cached locally and are not fetched during model execution. Final subjects: cream/red-roof house, gray/blue data center, green mature tree, young sapling, navy solar array, blue/green recycled-water symbol and small brown deer.

The common direction was an original classic pixel-art simulation game style: stepped square-pixel contours, readable dark outlines, elevated three-quarter views, limited palettes, one isolated object, no text/scenery/ground tile, and a genuine transparent background. The leader's supplied image was a visual direction, not a source texture copied into this model.

Full generation prompts and original generated-file provenance are in [generation-prompts.json](generation-prompts.json). The source PNGs are preserved at generated resolution; NetLogo scales them to display sizes through its bundled Bitmap extension. Houses use 28/30/32 px variants at lower counts and scale down for 50 homes; facilities use 48/40/32 px at 1–10/11–20/21–50, solar uses 44 px, trees 24/26/28 px, saplings/deer 20 px and recycling 24 px. Solar also uses a compact 24 px legend sprite. No background removal or edited existing artwork was substituted.

The `numbers/` directory contains 56 transparent, proportional-width glyph PNGs (`0123456789.%/-`, four value colors), rendered with Java's standard AWT font/drawing library by `../tests/CreateNumberGlyphs.java`. These are simple UI typography assets, not generated illustrations. They let the model draw large live values while keeping patch labels at 16 px. No runtime Java extension was added.

The earlier Icons8 Fluency artwork remains in V2.1 and original V2, with its attribution. V2.2 renders the original set described here. Terrain, roads, bridge, card colors and bars are native NetLogo patch/drawing content. Artwork and numbers do not create simulation agents.
