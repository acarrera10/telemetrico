# Dashboard v0.4.3 — prototype mapping

Source of truth: approved 1600×900 HTML/CSS prototype supplied for the normal race state.

## Geometry

- Canvas: 1600×900
- Header: y=0..116
- Main body: y=126..753
- Position: x=0, w=344
- Drive: x=356, w=680
- Systems: x=1047, w=553
- Bottom strip: y=766..900

The Android layout preserves these logical coordinates and scales the complete canvas proportionally using `min(viewWidth/1600, viewHeight/900)`. Modules do not reflow.

## Dynamic telemetry mapping

- Position, lap, gaps: live telemetry
- Gear, speed, throttle, brake: live telemetry
- Rev lights: 15 dynamic LEDs driven by `revLightsBits`; if the bitfield is unavailable, the existing `revLightsPercent` fallback derives the number of illuminated LEDs. The 1–15 numbers below them are only fixed visual scale labels.
- DRS: active / available / activation distance
- Fuel: percentage + estimated laps
- ERS: percentage + deployment mode
- Tyres: compound, age/life, four temperatures and wear values
- Timing: current/last/best lap and S1/S2/S3 deltas
- Speed trap and penalties: live telemetry

## Assets still to integrate on this branch

- Nimbus Sans / Nimbus Sans Narrow OTF files from the supplied bundle
- Exact approved monoplaza SVG as Android vector asset
- Team logos mapped by `teamId` when supplied
