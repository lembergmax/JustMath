# Unit constant audit

JustMath 1.7.0 ships 601 units in 13 groups. Before 1.7.0 the scale factors came from a converter website and nothing in the repository said where a value came from. The audit in #162 checked every constant against a definition. This page records the method, the 66 values that changed and the 89 units that no definition fixes.

| | Units |
| --- | --- |
| Checked against a definition | 512 |
| Corrected in 1.7.0 | 66 |
| Listed as an exception, with a reason | 89 |

## How a unit is checked

Two files in `src/test/resources/unit-audit/` classify every unit, and `UnitDefinitionAuditTest` reads them.

- `unit-definitions.tsv` holds the value of 1 unit in the base unit of its group, derived from the defining constants, and the source of the definition. A value is one of three kinds:
  - `EXACT`: a ratio of integers or a terminating decimal, for example `4046.8564224` m² for the acre or `8896443230521/1290320000` Pa for the psi. The test allows a relative error of 1E-100.
  - `DECIMAL`: a measured value with the digits that the source states, for example the CODATA value of the electron mass. The test compares the digits exactly.
  - `IRRATIONAL`: a value that contains pi, which is the parsec family. The test allows a relative error of 1E-40.
- `unit-exceptions.tsv` holds the units that no definition fixes, with the reason.

The test fails if a unit is in neither file, in both files, or if a value in the registry differs from its definition. A new unit therefore needs a definition or a written reason before the build passes.

The `EXACT` values are derived from a small set of defining constants, never copied from the registry:

| Constant | Value | Source |
| --- | --- | --- |
| inch | 0.0254 m | International yard and pound agreement (1959) |
| pound | 0.45359237 kg | International yard and pound agreement (1959) |
| standard gravity | 9.80665 m/s² | 3rd CGPM (1901) |
| US gallon | 231 in³ | NIST Handbook 44, Appendix C |
| imperial gallon | 4.54609 L | Weights and Measures Act 1985 |
| calorie (IT), calorie (th) | 4.1868 J, 4.184 J | International Steam Table, thermochemical definition |
| Julian year | 31557600 s | IAU |
| astronomical unit | 149597870700 m | IAU 2012 Resolution B2 |
| speed of light | 299792458 m/s | SI |
| elementary charge | 1.602176634E-19 C | SI |

Physical constants use CODATA 2022 from [NIST](https://physics.nist.gov/cuu/Constants/Table/allascii.txt). The Earth radii use the WGS 84 ellipsoid (semi-major axis 6378137 m, flattening 1/298.257223563). The solar radius is the IAU 2015 Resolution B3 nominal value. The masses of the Earth and the Sun come from the nominal GM values of the same resolution divided by the CODATA 2022 gravitational constant.

## What changed

Most corrections are values that were rounded to 8 to 14 digits although the definition is an exact ratio (pound-force, psi, horsepower, foot-pound, torr and their relatives). These change a result by less than 1E-9 relative. The larger changes are listed first.

| Change | Unit | Before | After |
| --- | --- | --- | --- |
| wrong value | Ken | 2.11836 m | 20/11 m (six shaku, 1 shaku = 10/33 m) |
| outdated constant | Sun's mass | 2.0E+30 kg | 1.98841E+30 kg |
| outdated constant | Earth's mass | 5.976E+24 kg | 5.9722E+24 kg |
| rounded value | Sun radius | 696000000 m | 695700000 m |
| outdated constant | Planck length, Planck mass, Planck time | values 1.3E-4 off | CODATA 2022 |
| two values for one unit | dalton and unified atomic mass unit | 1.66053E-27 kg and 1.6605402E-27 kg | both 1.66053906892E-27 kg |
| wrong definition | Btu (th) | 1054.3499999744 J | 1054.35026448889 J (453.59237 g x 4.184 J/g/K x 5/9 K) |
| outdated constant | Earth equatorial and polar radius | 6378160 m, 6356777 m | 6378137 m, 6356752.314245 m (WGS 84) |

The complete list, sorted by group:

| Unit | Symbol | Old value | New value | Change |
| --- | --- | --- | --- | --- |
| Thomson Cross Section (`Area.ELECTRON_CROSS_SECTION`) | `σT` | 6.652461599E-29 | 6.6524587051E-29 | 4.4e-07 |
| Homestead (`Area.HOMESTEAD`) | `hstd` | 647497.02758 | 647497.027584 | 6.2e-12 |
| Township (`Area.TOWNSHIP`) | `twp` | 93239571.972 | 93239571.9721 | 1.0e-12 |
| Btu (th) (`Energy.BTU_TH`) | `Btu(th)` | 1054.34999997 | 1054.35026449 | 2.5e-07 |
| Foot-Pound (`Energy.FOOT_POUND`) | `ft*lbf` | 1.3558179483 | 1.35581794833 | 2.3e-11 |
| Hartree Energy (`Energy.HARTREE_ENERGY`) | `Eh` | 4.3597482E-18 | 4.35974472221E-18 | 8.0e-07 |
| Horsepower Hour (`Energy.HORSEPOWER_HOUR`) | `hp*h` | 2684519.53689 | 2684519.53770 | 3.0e-10 |
| Inch-Ounce (`Energy.INCH_OUNCE`) | `in*ozf` | 0.0070615518 | 0.00706155181423 | 2.0e-09 |
| Inch-Pound (`Energy.INCH_POUND`) | `in*lbf` | 0.112984829 | 0.112984829028 | 2.4e-10 |
| Ounce-Force Inch (`Energy.OUNCE_FORCE_INCH`) | `ozf*in` | 0.0070615518 | 0.00706155181423 | 2.0e-09 |
| Poundal Foot (`Energy.POUNDAL_FOOT`) | `pdl*ft` | 0.04214011 | 0.0421401100938 | 2.2e-09 |
| Pound-Force Foot (`Energy.POUND_FORCE_FOOT`) | `lbf*ft` | 1.3558179483 | 1.35581794833 | 2.3e-11 |
| Pound-Force Inch (`Energy.POUND_FORCE_INCH`) | `lbf*in` | 0.112984829 | 0.112984829028 | 2.4e-10 |
| Rydberg Constant (`Energy.RYDBERG_CONSTANT`) | `Ry` | 2.1798741E-18 | 2.17987236110E-18 | 8.0e-07 |
| Kilopound-Force (`Force.KILOPOUND_FORCE`) | `kipf` | 4448.22161525 | 4448.22161526 | 1.3e-12 |
| Kip-Force (`Force.KIP_FORCE`) | `klbf` | 4448.22161525 | 4448.22161526 | 1.3e-12 |
| Ounce-Force (`Force.OUNCE_FORCE`) | `ozf` | 0.278013851 | 0.278013850954 | 1.7e-10 |
| Poundal (`Force.POUNDAL`) | `pdl` | 0.1382549544 | 0.138254954376 | 1.7e-10 |
| Pound Foot per Square Second (`Force.POUND_FOOT_PER_SQUARE_SECOND`) | `pound foot/square second` | 0.1382549544 | 0.138254954376 | 1.7e-10 |
| Pound-Force (`Force.POUND_FORCE`) | `lbf` | 4.4482216153 | 4.44822161526 | 8.9e-12 |
| Ton-Force (Long) (`Force.TON_FORCE_LONG`) | `tonf (UK)` | 9964.01641817 | 9964.01641818 | 1.3e-12 |
| Bohr Radius (`Length.BOHR_RADIUS`) | `a0` | 5.29177249E-11 | 5.29177210544E-11 | 7.3e-08 |
| Earth Equatorial Radius (`Length.EARTH_EQUATORIAL_RADIUS`) | `R_earth_eq` | 6378160 | 6378137 | 3.6e-06 |
| Earth Polar Radius (`Length.EARTH_POLAR_RADIUS`) | `R_earth_p` | 6356777 | 6356752.31424 | 3.9e-06 |
| Electron Radius (`Length.ELECTRON_RADIUS`) | `re` | 2.81794092E-15 | 2.8179403205E-15 | 2.1e-07 |
| Ken (`Length.KEN`) | `ken` | 2.11836 | 1.81818181818 | 1.7e-01 |
| Planck Length (`Length.PLANCK_LENGTH`) | `lP` | 1.616049999E-35 | 1.616255E-35 | 1.3e-04 |
| Sun Radius (`Length.SUN_RADIUS`) | `Rsun` | 696000000 | 695700000 | 4.3e-04 |
| X Unit (`Length.X_UNIT`) | `xu` | 1.002079999E-13 | 1.00207697E-13 | 3.0e-06 |
| Atomic Mass Unit (`Mass.ATOMIC_MASS_UNIT`) | `u` | 1.6605402E-27 | 1.66053906892E-27 | 6.8e-07 |
| Dalton (`Mass.DALTON`) | `Da` | 1.66053000000E-27 | 1.66053906892E-27 | 5.5e-06 |
| Deuteron Mass (`Mass.DEUTERON_MASS`) | `deuteron_mass` | 3.343586E-27 | 3.3435837768E-27 | 6.6e-07 |
| Earth's Mass (`Mass.EARTH_MASS`) | `earth_mass` | 5.97600000000E24 | 5.97220000000E24 | 6.4e-04 |
| Electron Mass (Rest) (`Mass.ELECTRON_REST_MASS`) | `electron_rest_mass` | 9.1093897E-31 | 9.1093837139E-31 | 6.6e-07 |
| Muon Mass (`Mass.MUON_MASS`) | `muon_mass` | 1.8835327E-28 | 1.883531627E-28 | 5.7e-07 |
| Neutron Mass (`Mass.NEUTRON_MASS`) | `neutron_mass` | 1.6749286E-27 | 1.67492750056E-27 | 6.6e-07 |
| Planck Mass (`Mass.PLANCK_MASS`) | `planck_mass` | 2.17671E-8 | 2.176434E-8 | 1.3e-04 |
| Pound-Force Square Second per Foot (`Mass.POUND_FORCE_SECOND_SQUARED_PER_FOOT`) | `lbf*s^2/ft` | 14.5939029372 | 14.5939029372 | 4.4e-13 |
| Proton Mass (`Mass.PROTON_MASS`) | `proton_mass` | 1.6726231E-27 | 1.67262192595E-27 | 7.0e-07 |
| Slug (`Mass.SLUG`) | `slug` | 14.5939029372 | 14.5939029372 | 4.4e-13 |
| Sun's Mass (`Mass.SUN_MASS`) | `sun_mass` | 2.00000000000E30 | 1.98841000000E30 | 5.8e-03 |
| Btu (th) per Hour (`Power.BTU_TH_PER_HOUR`) | `Btu(th)/h` | 0.292874999993 | 0.292875073469 | 2.5e-07 |
| Btu (th) per Minute (`Power.BTU_TH_PER_MINUTE`) | `Btu(th)/min` | 17.5724999996 | 17.5725044081 | 2.5e-07 |
| Btu (th) per Second (`Power.BTU_TH_PER_SECOND`) | `Btu(th)/s` | 1054.34999997 | 1054.35026449 | 2.5e-07 |
| Foot Pound-Force per Hour (`Power.FOOT_POUND_FORCE_PER_HOUR`) | `ft*lbf/h` | 0.00037661609675 | 0.000376616096759 | 2.3e-11 |
| Foot Pound-Force per Minute (`Power.FOOT_POUND_FORCE_PER_MINUTE`) | `ft*lbf/min` | 0.022596965805 | 0.0225969658055 | 2.3e-11 |
| Foot Pound-Force per Second (`Power.FOOT_POUND_FORCE_PER_SECOND`) | `ft*lbf/s` | 1.3558179483 | 1.35581794833 | 2.3e-11 |
| Horsepower (`Power.HORSEPOWER`) | `hp` | 745.699871582 | 745.699871582 | 4.0e-14 |
| Horsepower (550 ft*lbf/s) (`Power.HORSEPOWER_MECHANICAL_550_FTLBF_PER_S`) | `hp(550ft*lbf/s)` | 745.699871582 | 745.699871582 | 4.0e-14 |
| Pound-Foot per Hour (`Power.POUND_FOOT_PER_HOUR`) | `lbf*ft/h` | 0.00037661609675 | 0.000376616096759 | 2.3e-11 |
| Pound-Foot per Minute (`Power.POUND_FOOT_PER_MINUTE`) | `lbf*ft/min` | 0.022596965805 | 0.0225969658055 | 2.3e-11 |
| Pound-Foot per Second (`Power.POUND_FOOT_PER_SECOND`) | `lbf*ft/s` | 1.3558179483 | 1.35581794833 | 2.3e-11 |
| Ton (Refrigeration) (`Power.TON_REFRIGERATION`) | `TR` | 3516.85284207 | 3516.85284207 | 9.5e-15 |
| Kip-Force per Square Inch (`Pressure.KIP_FORCE_PER_SQUARE_INCH`) | `kipf/in^2` | 6894757.29318 | 6894757.29317 | 1.4e-12 |
| Kips per Square Inch (`Pressure.KSI`) | `ksi` | 6894757.29318 | 6894757.29317 | 1.4e-12 |
| Ton-Force (Long) per Square Foot (`Pressure.LONG_TON_FORCE_PER_SQUARE_FOOT`) | `tonf(long)/ft^2` | 107251.780116 | 107251.780116 | 2.1e-14 |
| Ton-Force (Long) per Square Inch (`Pressure.LONG_TON_FORCE_PER_SQUARE_INCH`) | `tonf(long)/in^2` | 15444256.3367 | 15444256.3367 | 8.4e-15 |
| Poundal per Square Foot (`Pressure.POUNDAL_PER_SQUARE_FOOT`) | `pdl/ft^2` | 1.4881639436 | 1.48816394357 | 2.0e-11 |
| Pound-Force per Square Foot (`Pressure.POUND_FORCE_PER_SQUARE_FOOT`) | `lbf/ft^2` | 47.8802589804 | 47.8802589803 | 1.3e-12 |
| Pound-Force per Square Inch (`Pressure.POUND_FORCE_PER_SQUARE_INCH`) | `lbf/in^2` | 6894.75729318 | 6894.75729317 | 1.4e-12 |
| Pounds per Square Inch (`Pressure.PSI`) | `psi` | 6894.75729318 | 6894.75729317 | 1.4e-12 |
| Ton-Force (Short) per Square Foot (`Pressure.SHORT_TON_FORCE_PER_SQUARE_FOOT`) | `tonf(short)/ft^2` | 95760.5179607 | 95760.5179607 | 6.6e-14 |
| Ton-Force (Short) per Square Inch (`Pressure.SHORT_TON_FORCE_PER_SQUARE_INCH`) | `tonf(short)/in^2` | 13789514.5863 | 13789514.5863 | 9.3e-14 |
| Torr (`Pressure.TORR`) | `Torr` | 133.322368421 | 133.322368421 | 3.6e-13 |
| Planck Time (`Time.PLANCK_TIME`) | `Planck time` | 5.39056E-44 | 5.391247E-44 | 1.3e-04 |
| Earth's Volume (`Volume.EARTH_VOLUME`) | `earth_volume` | 1.08300000000E21 | 1.08321000000E21 | 1.9e-04 |

## Units without a definition

The 89 exceptions are in `unit-exceptions.tsv`. They fall into a few groups.

- Historic and regional units (biblical and Roman measures, the Swedish famn and aln, the arpent, the cuerda, the Spanish taza). The value is the one the registry always had. It was not checked against a primary source.
- Values that NIST SP 811 publishes with six significant digits: the columns of mercury and of water, the boiler and water horsepower, the tropical year.
- Conventions: word and block sizes, the nominal capacity of floppy disks, Zip and Jaz disks, CDs and DVDs.
- Approximations of astronomical and acoustic values: the cosmic velocities, the speed of sound, the Mach numbers, the synodic and sidereal time units.

Three entries are known to be inconsistent and stay as they are until someone can name a source that settles them.

- `FUEL_OIL_EQUIVALENT_KILOLITER` and `FUEL_OIL_EQUIVALENT_US_BARREL` differ by 0.12 % when you compare them through the volume of a US barrel. The energy content of a fuel varies, so no definition decides which one is right.
- `MONTH` is a twelfth of a 365 day year, while `YEAR` is the Julian year of 365.25 days. The same 365 day year sits behind `SEPTENNIAL`, `OCTENNIAL`, `NOVENNIAL`, `QUINDECENNIAL` and `QUINQUENNIAL`.
- `THERM` and `THERM_EC` are 105505600 J, the value the UK gas industry uses. One hundred thousand Btu (IT) would be 105505585.262 J.

## Adding a unit

1. Add the enum constant to `Unit` and one `define` line to `UnitRegistry.BUILT_IN`. Use `defineFraction` when the definition is a ratio without a finite decimal form.
2. Add the unit to `unit-definitions.tsv` with the value that the definition gives and the source, or to `unit-exceptions.tsv` with the reason.
3. Run `./mvnw test -Dtest=UnitDefinitionAuditTest`.
