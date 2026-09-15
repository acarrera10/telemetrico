Asset integration note for dashboard rebuild.

The approved HTML/CSS bundle is the visual source of truth. Binary font assets are intentionally not duplicated through the text-only repository connector; they must be added as Android font resources from the supplied bundle before the dashboard branch is released. The exact monoplaza SVG remains the source asset for conversion to Android VectorDrawable.
