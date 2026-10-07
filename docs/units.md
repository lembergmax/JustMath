# Units reference

This file is generated from the unit registry by `ReferenceDocumentsTest`. Do not edit it by hand: run
`./mvnw test -Dtest=ReferenceDocumentsTest -Dupdate.docs=true` after you change a unit.

JustMath has 601 units in 13 groups. A conversion is only defined inside a group: it goes through the base unit of the group.

The value column is the value of one unit in the base unit, rounded to 12 significant digits for this table. The registry holds the exact definition. For a temperature, the column is the value of one unit in degrees Celsius; for a unit of fuel consumption that is written as a quantity per distance, such as `L/100 km`, it is the value of the reciprocal form.

The basis column says where the value comes from. *exact* is a definition (SI, the international yard and pound, US customary or imperial measures) that the registry holds as an exact ratio, *measured* is a physical or astronomical constant with the digits of the source (CODATA 2022, WGS 84, IAU), *exact (pi)* contains pi, and *convention* is a historic, regional or rounded value without a definition. [unit-audit.md](unit-audit.md) lists the sources and the units of the last class with the reason.

| Group | Units | Base unit |
| --- | ---: | --- |
| [Length](#length) | 72 | Meter (`m`) |
| [Area](#area) | 29 | Square Meter (`m^2`) |
| [Volume](#volume) | 73 | Cubic Meter (`m^3`) |
| [Mass](#mass) | 66 | Kilogram (`kg`) |
| [Temperature](#temperature) | 3 | Celsius (`°C`) |
| [Pressure](#pressure) | 52 | Pascal (`Pa`) |
| [Energy](#energy) | 54 | Joule (`J`) |
| [Power](#power) | 76 | Watt (`W`) |
| [Time](#time) | 33 | Second (`s`) |
| [Force](#force) | 33 | Newton (`N`) |
| [Speed](#speed) | 32 | Meter per Second (`m/s`) |
| [FuelConsumption](#fuelconsumption) | 37 | Meter per Liter (`m/L`) |
| [DataStorage](#datastorage) | 41 | Bit (`bit`) |

## Length

Enum `Unit.Length`, base unit Meter (`m`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `EXAMETER` | Exameter | `Em` | 1E18 | exact |
| `PETAMETER` | Petameter | `Pm` | 1E15 | exact |
| `TERAMETER` | Terameter | `Tm` | 1E12 | exact |
| `GIGAMETER` | Gigameter | `Gm` | 1E9 | exact |
| `MEGAMETER` | Megameter | `Mm` | 1000000 | exact |
| `KILOMETER` | Kilometer | `km` | 1000 | exact |
| `HECTOMETER` | Hectometer | `hm` | 100 | exact |
| `DEKAMETER` | Dekameter | `dam` | 10 | exact |
| `METER` | Meter | `m` | 1 | exact |
| `DECIMETER` | Decimeter | `dm` | 0.1 | exact |
| `CENTIMETER` | Centimeter | `cm` | 0.01 | exact |
| `MILLIMETER` | Millimeter | `mm` | 0.001 | exact |
| `MICROMETER` | Micrometer | `µm` | 0.000001 | exact |
| `MICRON` | Micron | `um` | 0.000001 | exact |
| `NANOMETER` | Nanometer | `nm` | 1E-9 | exact |
| `ANGSTROM` | Angstrom | `Å` | 1E-10 | exact |
| `PICOMETER` | Picometer | `pm` | 1E-12 | exact |
| `FEMTOMETER` | Femtometer | `fm` | 1E-15 | exact |
| `ATTOMETER` | Attometer | `am` | 1E-18 | exact |
| `PLANCK_LENGTH` | Planck Length | `lP` | 1.616255E-35 | measured |
| `ELECTRON_RADIUS` | Electron Radius | `re` | 2.8179403205E-15 | measured |
| `BOHR_RADIUS` | Bohr Radius | `a0` | 5.29177210544E-11 | measured |
| `X_UNIT` | X Unit | `xu` | 1.00207697E-13 | measured |
| `FERMI` | Fermi | `fermi` | 1E-15 | exact |
| `SUN_RADIUS` | Sun Radius | `Rsun` | 695700000 | exact |
| `EARTH_EQUATORIAL_RADIUS` | Earth Equatorial Radius | `R_earth_eq` | 6378137 | measured |
| `EARTH_POLAR_RADIUS` | Earth Polar Radius | `R_earth_p` | 6356752.31424 | measured |
| `ASTRONOMICAL_UNIT` | Astronomical Unit | `au` | 1.495978707E11 | exact |
| `EARTH_DISTANCE_FROM_SUN` | Earth Distance from Sun | `AU` | 1.496E11 | convention |
| `KILOPARSEC` | Kiloparsec | `kpc` | 3.08567758149E19 | exact (pi) |
| `MEGAPARSEC` | Megaparsec | `Mpc` | 3.08567758149E22 | exact (pi) |
| `PARSEC` | Parsec | `pc` | 3.08567758149E16 | exact (pi) |
| `LIGHT_YEAR` | Light Year | `ly` | 9.46073047258E15 | exact |
| `LEAGUE` | League | `lea` | 4828.032 | exact |
| `NAUTICAL_LEAGUE_INTERNATIONAL` | Nautical League | `NL` | 5556 | exact |
| `NAUTICAL_LEAGUE_UK` | Nautical League (UK) | `NL (UK)` | 5559.552 | exact |
| `NAUTICAL_MILE` | Nautical Mile | `nmi` | 1852 | exact |
| `NAUTICAL_MILE_UK` | Nautical Mile (UK) | `nmi (UK)` | 1853.184 | exact |
| `MILE` | Mile | `mi` | 1609.344 | exact |
| `MILE_ROMAN` | Roman Mile | `m.p.` | 1479.804 | convention |
| `KILOYARD` | Kiloyard | `kyd` | 914.4 | exact |
| `FURLONG` | Furlong | `fur` | 201.168 | exact |
| `CHAIN` | Chain | `ch` | 20.1168 | exact |
| `ROPE` | Rope | `rope` | 6.096 | exact |
| `ROD` | Rod | `rod` | 5.0292 | exact |
| `FATHOM` | Fathom | `ftm` | 1.8288 | exact |
| `FAMN` | Famn | `famn` | 1.7813333333 | convention |
| `ELL` | Ell | `ell` | 1.143 | exact |
| `ALN` | Aln | `aln` | 0.5937777778 | convention |
| `CUBIT_UK` | Cubit (UK) | `cubit` | 0.4572 | exact |
| `SPAN_CLOTH` | Span (cloth) | `span` | 0.2286 | exact |
| `LINK` | Link | `li` | 0.201168 | exact |
| `FINGER_CLOTH` | Finger (cloth) | `finger` | 0.1143 | exact |
| `HAND` | Hand | `hand` | 0.1016 | exact |
| `HANDBREADTH` | Handbreadth | `hb` | 0.0762 | exact |
| `NAIL_CLOTH` | Nail (cloth) | `nail` | 0.05715 | exact |
| `FINGERBREADTH` | Fingerbreadth | `fb` | 0.01905 | exact |
| `BARLEYCORN` | Barleycorn | `barleycorn` | 0.00846666666667 | exact |
| `YARD` | Yard | `yd` | 0.9144 | exact |
| `FEET` | Foot | `ft` | 0.3048 | exact |
| `INCH` | Inch | `in` | 0.0254 | exact |
| `CENTIINCH` | Centiinch | `cin` | 0.000254 | exact |
| `CALIBER` | Caliber | `cl` | 0.000254 | exact |
| `MIL` | Mil | `mil` | 0.0000254 | exact |
| `MICROINCH` | Microinch | `µin` | 2.54E-8 | exact |
| `ARPENT` | Arpent | `arp` | 58.5216 | exact |
| `KEN` | Ken | `ken` | 1.81818181818 | exact |
| `PIXEL` | Pixel | `px` | 0.000264583333333 | exact |
| `POINT` | Point | `pt` | 0.000352777777778 | exact |
| `PICA` | Pica | `pica` | 0.00423333333333 | exact |
| `EM` | Em | `em` | 0.00423333333333 | exact |
| `TWIP` | Twip | `twip` | 0.0000176388888889 | exact |

## Area

Enum `Unit.Area`, base unit Square Meter (`m^2`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `SQUARE_KILOMETER` | Square Kilometer | `km^2` | 1000000 | exact |
| `SQUARE_HECTOMETER` | Square Hectometer | `hm^2` | 10000 | exact |
| `SQUARE_DEKAMETER` | Square Dekameter | `dam^2` | 100 | exact |
| `SQUARE_METER` | Square Meter | `m^2` | 1 | exact |
| `SQUARE_DECIMETER` | Square Decimeter | `dm^2` | 0.01 | exact |
| `SQUARE_CENTIMETER` | Square Centimeter | `cm^2` | 0.0001 | exact |
| `SQUARE_MILLIMETER` | Square Millimeter | `mm^2` | 0.000001 | exact |
| `SQUARE_MICROMETER` | Square Micrometer | `µm^2` | 1E-12 | exact |
| `SQUARE_NANOMETER` | Square Nanometer | `nm^2` | 1E-18 | exact |
| `HECTARE` | Hectare | `ha` | 10000 | exact |
| `ARE` | Are | `a` | 100 | exact |
| `BARN` | Barn | `b` | 1E-28 | exact |
| `ELECTRON_CROSS_SECTION` | Thomson Cross Section | `σT` | 6.6524587051E-29 | measured |
| `TOWNSHIP` | Township | `twp` | 93239571.9721 | exact |
| `SECTION` | Section | `sec` | 2589988.11034 | exact |
| `HOMESTEAD` | Homestead | `hstd` | 647497.027584 | exact |
| `SQUARE_MILE` | Square Mile | `mi^2` | 2589988.11034 | exact |
| `ACRE` | Acre | `ac` | 4046.8564224 | exact |
| `ROOD` | Rood | `rood` | 1011.7141056 | exact |
| `SQUARE_CHAIN` | Square Chain | `ch^2` | 404.68564224 | exact |
| `SQUARE_ROD` | Square Rod | `rd^2` | 25.29285264 | exact |
| `SQUARE_POLE` | Square Pole | `pole^2` | 25.29285264 | exact |
| `SQUARE_ROPE` | Square Rope | `rope^2` | 37.161216 | exact |
| `SQUARE_YARD` | Square Yard | `yd^2` | 0.83612736 | exact |
| `SQUARE_FOOT` | Square Foot | `ft^2` | 0.09290304 | exact |
| `SQUARE_INCH` | Square Inch | `in^2` | 0.00064516 | exact |
| `ARPENT` | Arpent | `arp_area` | 3418.8929237 | convention |
| `CUERDA` | Cuerda | `cda` | 3930.395625 | convention |
| `PLAZA` | Plaza | `plz` | 6400 | convention |

## Volume

Enum `Unit.Volume`, base unit Cubic Meter (`m^3`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `CUBIC_KILOMETER` | Cubic Kilometer | `km^3` | 1E9 | exact |
| `CUBIC_METER` | Cubic Meter | `m^3` | 1 | exact |
| `CUBIC_DECIMETER` | Cubic Decimeter | `dm^3` | 0.001 | exact |
| `CUBIC_CENTIMETER` | Cubic Centimeter | `cm^3` | 0.000001 | exact |
| `CUBIC_MILLIMETER` | Cubic Millimeter | `mm^3` | 1E-9 | exact |
| `EXALITER` | Exaliter | `EL` | 1E15 | exact |
| `PETALITER` | Petaliter | `PL` | 1E12 | exact |
| `TERALITER` | Teraliter | `TL` | 1E9 | exact |
| `GIGALITER` | Gigaliter | `GL` | 1000000 | exact |
| `MEGALITER` | Megaliter | `ML` | 1000 | exact |
| `KILOLITER` | Kiloliter | `kL` | 1 | exact |
| `HECTOLITER` | Hectoliter | `hL` | 0.1 | exact |
| `DEKALITER` | Dekaliter | `daL` | 0.01 | exact |
| `LITER` | Liter | `L` | 0.001 | exact |
| `DECILITER` | Deciliter | `dL` | 0.0001 | exact |
| `CENTILITER` | Centiliter | `cL` | 0.00001 | exact |
| `MILLILITER` | Milliliter | `mL` | 0.000001 | exact |
| `MICROLITER` | Microliter | `µL` | 1E-9 | exact |
| `NANOLITER` | Nanoliter | `nL` | 1E-12 | exact |
| `PICOLITER` | Picoliter | `pL` | 1E-15 | exact |
| `FEMTOLITER` | Femtoliter | `fL` | 1E-18 | exact |
| `ATTOLITER` | Attoliter | `aL` | 1E-21 | exact |
| `METRIC_CUP` | Cup (Metric) | `cup_metric` | 0.00025 | exact |
| `METRIC_TABLESPOON` | Tablespoon (Metric) | `tbsp_metric` | 0.000015 | exact |
| `METRIC_TEASPOON` | Teaspoon (Metric) | `tsp_metric` | 0.000005 | exact |
| `US_GALLON` | Gallon (United States) | `gal_us` | 0.003785411784 | exact |
| `US_QUART` | Quart (United States) | `qt_us` | 0.000946352946 | exact |
| `US_PINT` | Pint (United States) | `pt_us` | 0.000473176473 | exact |
| `US_CUP` | Cup (United States) | `cup_us` | 0.0002365882365 | exact |
| `US_FLUID_OUNCE` | Fluid Ounce (United States) | `floz_us` | 0.0000295735295625 | exact |
| `US_TABLESPOON` | Tablespoon (United States) | `tbsp_us` | 0.0000147867647812 | exact |
| `US_DESSERTSPOON` | Dessertspoon (United States) | `dsp_us` | 0.0000098578431875 | exact |
| `US_TEASPOON` | Teaspoon (United States) | `tsp_us` | 0.00000492892159375 | exact |
| `US_GILL` | Gill (United States) | `gi_us` | 0.00011829411825 | exact |
| `US_MINIM` | Minim (United States) | `minim_us` | 6.16115199219E-8 | exact |
| `US_BARREL` | Barrel (United States) | `bbl_us` | 0.119240471196 | exact |
| `IMPERIAL_GALLON` | Gallon (United Kingdom) | `gal_uk` | 0.00454609 | exact |
| `IMPERIAL_QUART` | Quart (United Kingdom) | `qt_uk` | 0.0011365225 | exact |
| `IMPERIAL_PINT` | Pint (United Kingdom) | `pt_uk` | 0.00056826125 | exact |
| `IMPERIAL_CUP` | Cup (United Kingdom) | `cup_uk` | 0.000284130625 | exact |
| `IMPERIAL_FLUID_OUNCE` | Fluid Ounce (United Kingdom) | `floz_uk` | 0.0000284130625 | exact |
| `IMPERIAL_TABLESPOON` | Tablespoon (United Kingdom) | `tbsp_uk` | 0.0000177581640625 | exact |
| `IMPERIAL_DESSERTSPOON` | Dessertspoon (United Kingdom) | `dsp_uk` | 0.0000118387760417 | exact |
| `IMPERIAL_TEASPOON` | Teaspoon (United Kingdom) | `tsp_uk` | 0.00000591938802083 | exact |
| `IMPERIAL_GILL` | Gill (United Kingdom) | `gi_uk` | 0.0001420653125 | exact |
| `IMPERIAL_MINIM` | Minim (United Kingdom) | `minim_uk` | 5.91938802083E-8 | exact |
| `IMPERIAL_BARREL` | Barrel (United Kingdom) | `bbl_uk` | 0.16365924 | exact |
| `CUBIC_MILE` | Cubic Mile | `mi^3` | 4168181825.44 | exact |
| `CUBIC_YARD` | Cubic Yard | `yd^3` | 0.764554857984 | exact |
| `CUBIC_FOOT` | Cubic Foot | `ft^3` | 0.028316846592 | exact |
| `CUBIC_INCH` | Cubic Inch | `in^3` | 0.000016387064 | exact |
| `HUNDRED_CUBIC_FOOT` | Hundred Cubic Foot | `hundred_cubic_foot` | 2.8316846592 | exact |
| `TON_REGISTER` | Ton Register | `ton_reg` | 2.8316846592 | exact |
| `ACRE_FOOT` | Acre-Foot | `ac*ft` | 1233.48183755 | exact |
| `ACRE_INCH` | Acre-Inch | `ac*in` | 102.790153129 | exact |
| `BOARD_FOOT` | Board Foot | `board_foot` | 0.002359737216 | exact |
| `STERE` | Stere | `stere` | 1 | exact |
| `DEKASTERE` | Dekastere | `dekastere` | 10 | exact |
| `DECISTERE` | Decistere | `decistere` | 0.1 | exact |
| `CORD` | Cord | `cord` | 3.62455636378 | exact |
| `DROP` | Drop | `drop` | 5E-8 | exact |
| `OIL_BARREL` | Barrel (Oil) | `bbl_oil` | 0.158987294928 | exact |
| `TUN` | Tun | `tun` | 0.953923769568 | exact |
| `HOGSHEAD` | Hogshead | `hogshead` | 0.238480942392 | exact |
| `DRAM` | Dram | `dr` | 0.00000369669119531 | exact |
| `SPANISH_TAZA` | Taza (Spanish) | `taza` | 0.0002365882365 | convention |
| `BIBLICAL_COR` | Cor (Biblical) | `cor_biblical` | 0.22 | convention |
| `BIBLICAL_HOMER` | Homer (Biblical) | `homer_biblical` | 0.22 | convention |
| `BIBLICAL_BATH` | Bath (Biblical) | `bath_biblical` | 0.022 | convention |
| `BIBLICAL_HIN` | Hin (Biblical) | `hin_biblical` | 0.00366666666667 | convention |
| `BIBLICAL_CAB` | Cab (Biblical) | `cab_biblical` | 0.00122222222222 | convention |
| `BIBLICAL_LOG` | Log (Biblical) | `log_biblical` | 0.000305555555556 | convention |
| `EARTH_VOLUME` | Earth's Volume | `earth_volume` | 1.08321E21 | measured |

## Mass

Enum `Unit.Mass`, base unit Kilogram (`kg`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `EXAGRAM` | Exagram | `Eg` | 1E15 | exact |
| `PETAGRAM` | Petagram | `Pg` | 1E12 | exact |
| `TERAGRAM` | Teragram | `Tg` | 1E9 | exact |
| `GIGAGRAM` | Gigagram | `Gg` | 1000000 | exact |
| `MEGAGRAM` | Megagram | `Mg` | 1000 | exact |
| `KILOTON` | Kiloton (Metric) | `kt` | 1000000 | exact |
| `QUINTAL_METRIC` | Quintal (Metric) | `q` | 100 | exact |
| `TON` | Tonne (Metric) | `t` | 1000 | exact |
| `KILOGRAM` | Kilogram | `kg` | 1 | exact |
| `HECTOGRAM` | Hectogram | `hg` | 0.1 | exact |
| `DEKAGRAM` | Dekagram | `dag` | 0.01 | exact |
| `GRAM` | Gram | `g` | 0.001 | exact |
| `DECIGRAM` | Decigram | `dg` | 0.0001 | exact |
| `CENTIGRAM` | Centigram | `cg` | 0.00001 | exact |
| `MILLIGRAM` | Milligram | `mg` | 0.000001 | exact |
| `MICROGRAM` | Microgram | `µg` | 1E-9 | exact |
| `GAMMA` | Gamma | `γ` | 1E-9 | exact |
| `NANOGRAM` | Nanogram | `ng` | 1E-12 | exact |
| `PICOGRAM` | Picogram | `pg` | 1E-15 | exact |
| `FEMTOGRAM` | Femtogram | `fg` | 1E-18 | exact |
| `ATTOGRAM` | Attogram | `ag` | 1E-21 | exact |
| `CARAT` | Carat | `ct` | 0.0002 | exact |
| `GRAIN` | Grain | `gr` | 0.00006479891 | exact |
| `PENNYWEIGHT` | Pennyweight | `dwt` | 0.00155517384 | exact |
| `SCRUPLE_APOTHECARY` | Scruple (Apothecary) | `℈` | 0.0012959782 | exact |
| `POUND_TROY_APOTHECARY` | Pound (Troy or Apothecary) | `lb_t` | 0.3732417216 | exact |
| `LONG_TON` | Ton (Long) | `LT` | 1016.0469088 | exact |
| `SHORT_TON` | Ton (Short) | `ST` | 907.18474 | exact |
| `POUND` | Pound | `lb` | 0.45359237 | exact |
| `OUNCE` | Ounce | `oz` | 0.028349523125 | exact |
| `HUNDREDWEIGHT_UNITED_STATES` | Hundredweight (United States) | `cwt(US)` | 45.359237 | exact |
| `HUNDREDWEIGHT_UNITED_KINGDOM` | Hundredweight (United Kingdom) | `cwt(UK)` | 50.80234544 | exact |
| `QUARTER_UNITED_STATES` | Quarter (United States) | `qr(US)` | 11.33980925 | exact |
| `QUARTER_UNITED_KINGDOM` | Quarter (United Kingdom) | `qr(UK)` | 12.70058636 | exact |
| `STONE_UNITED_STATES` | Stone (United States) | `st(US)` | 5.669904625 | convention |
| `STONE_UNITED_KINGDOM` | Stone (United Kingdom) | `st(UK)` | 6.35029318 | exact |
| `KILOGRAM_FORCE_SECOND_SQUARED_PER_METER` | Kilogram-Force Square Second per Meter | `kgf*s^2/m` | 9.80665 | exact |
| `POUND_FORCE_SECOND_SQUARED_PER_FOOT` | Pound-Force Square Second per Foot | `lbf*s^2/ft` | 14.5939029372 | exact |
| `SLUG` | Slug | `slug` | 14.5939029372 | exact |
| `KILOPOUND` | Kilopound | `klb` | 453.59237 | exact |
| `ASSAY_TON_UNITED_STATES` | Ton (Assay) (United States) | `AT(US)` | 0.0291666666667 | exact |
| `ASSAY_TON_UNITED_KINGDOM` | Ton (Assay) (United Kingdom) | `AT(UK)` | 0.0326666666667 | exact |
| `ATOMIC_MASS_UNIT` | Atomic Mass Unit | `u` | 1.66053906892E-27 | measured |
| `DALTON` | Dalton | `Da` | 1.66053906892E-27 | measured |
| `PLANCK_MASS` | Planck Mass | `planck_mass` | 2.176434E-8 | measured |
| `ELECTRON_REST_MASS` | Electron Mass (Rest) | `electron_rest_mass` | 9.1093837139E-31 | measured |
| `MUON_MASS` | Muon Mass | `muon_mass` | 1.883531627E-28 | measured |
| `PROTON_MASS` | Proton Mass | `proton_mass` | 1.67262192595E-27 | measured |
| `NEUTRON_MASS` | Neutron Mass | `neutron_mass` | 1.67492750056E-27 | measured |
| `DEUTERON_MASS` | Deuteron Mass | `deuteron_mass` | 3.3435837768E-27 | measured |
| `EARTH_MASS` | Earth's Mass | `earth_mass` | 5.9722E24 | measured |
| `SUN_MASS` | Sun's Mass | `sun_mass` | 1.98841E30 | measured |
| `BIBLICAL_HEBREW_TALENT` | Talent (Biblical Hebrew) | `talent_biblical_hebrew` | 34.2 | convention |
| `BIBLICAL_HEBREW_MINA` | Mina (Biblical Hebrew) | `mina_biblical_hebrew` | 0.57 | convention |
| `BIBLICAL_HEBREW_SHEKEL` | Shekel (Biblical Hebrew) | `shekel_biblical_hebrew` | 0.0114 | convention |
| `BIBLICAL_HEBREW_BEKAN` | Bekan (Biblical Hebrew) | `bekan_biblical_hebrew` | 0.0057 | convention |
| `BIBLICAL_HEBREW_GERAH` | Gerah (Biblical Hebrew) | `gerah_biblical_hebrew` | 0.00057 | convention |
| `BIBLICAL_GREEK_TALENT` | Talent (Biblical Greek) | `talent_biblical_greek` | 20.4 | convention |
| `BIBLICAL_GREEK_MINA` | Mina (Biblical Greek) | `mina_biblical_greek` | 0.34 | convention |
| `BIBLICAL_GREEK_TETRADRACHMA` | Tetradrachma (Biblical Greek) | `tetradrachma_biblical_greek` | 0.0136 | convention |
| `BIBLICAL_GREEK_DIDRACHMA` | Didrachma (Biblical Greek) | `didrachma_biblical_greek` | 0.0068 | convention |
| `BIBLICAL_GREEK_DRACHMA` | Drachma (Biblical Greek) | `drachma_biblical_greek` | 0.0034 | convention |
| `BIBLICAL_ROMAN_DENARIUS` | Denarius (Biblical Roman) | `denarius_biblical_roman` | 0.00385 | convention |
| `BIBLICAL_ROMAN_ASSARION` | Assarion (Biblical Roman) | `assarion_biblical_roman` | 0.000240625 | convention |
| `BIBLICAL_ROMAN_QUADRANS` | Quadrans (Biblical Roman) | `quadrans_biblical_roman` | 0.0000601563 | convention |
| `BIBLICAL_ROMAN_LEPTON` | Lepton (Biblical Roman) | `lepton_biblical_roman` | 0.0000300781 | convention |

## Temperature

Enum `Unit.Temperature`, base unit Celsius (`°C`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `KELVIN` | Kelvin | `K` | -272.15 | convention |
| `CELSIUS` | Celsius | `°C` | 1 | convention |
| `FAHRENHEIT` | Fahrenheit | `°F` | -17.2222222222 | convention |

## Pressure

Enum `Unit.Pressure`, base unit Pascal (`Pa`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `EXAPASCAL` | Exapascal | `EPa` | 1E18 | exact |
| `PETAPASCAL` | Petapascal | `PPa` | 1E15 | exact |
| `TERAPASCAL` | Terapascal | `TPa` | 1E12 | exact |
| `GIGAPASCAL` | Gigapascal | `GPa` | 1E9 | exact |
| `MEGAPASCAL` | Megapascal | `MPa` | 1000000 | exact |
| `KILOPASCAL` | Kilopascal | `kPa` | 1000 | exact |
| `HECTOPASCAL` | Hectopascal | `hPa` | 100 | exact |
| `DEKAPASCAL` | Dekapascal | `daPa` | 10 | exact |
| `PASCAL` | Pascal | `Pa` | 1 | exact |
| `DECIPASCAL` | Decipascal | `dPa` | 0.1 | exact |
| `CENTIPASCAL` | Centipascal | `cPa` | 0.01 | exact |
| `MILLIPASCAL` | Millipascal | `mPa` | 0.001 | exact |
| `MICROPASCAL` | Micropascal | `µPa` | 0.000001 | exact |
| `NANOPASCAL` | Nanopascal | `nPa` | 1E-9 | exact |
| `PICOPASCAL` | Picopascal | `pPa` | 1E-12 | exact |
| `FEMTOPASCAL` | Femtopascal | `fPa` | 1E-15 | exact |
| `ATTOPASCAL` | Attopascal | `aPa` | 1E-18 | exact |
| `BAR` | Bar | `bar` | 100000 | exact |
| `MILLIBAR` | Millibar | `mbar` | 100 | exact |
| `MICROBAR` | Microbar | `µbar` | 0.1 | exact |
| `STANDARD_ATMOSPHERE` | Standard Atmosphere | `atm` | 101325 | exact |
| `TECHNICAL_ATMOSPHERE` | Atmosphere (Technical) | `at` | 98066.5 | exact |
| `PSI` | Pounds per Square Inch | `psi` | 6894.75729317 | exact |
| `KSI` | Kips per Square Inch | `ksi` | 6894757.29317 | exact |
| `TORR` | Torr | `Torr` | 133.322368421 | exact |
| `NEWTON_PER_SQUARE_METER` | Newton per Square Meter | `N/m^2` | 1 | exact |
| `NEWTON_PER_SQUARE_CENTIMETER` | Newton per Square Centimeter | `N/cm^2` | 10000 | exact |
| `NEWTON_PER_SQUARE_MILLIMETER` | Newton per Square Millimeter | `N/mm^2` | 1000000 | exact |
| `KILONEWTON_PER_SQUARE_METER` | Kilonewton per Square Meter | `kN/m^2` | 1000 | exact |
| `DYNE_PER_SQUARE_CENTIMETER` | Dyne per Square Centimeter | `dyn/cm^2` | 0.1 | exact |
| `KILOGRAM_FORCE_PER_SQUARE_METER` | Kilogram-Force per Square Meter | `kgf/m^2` | 9.80665 | exact |
| `KILOGRAM_FORCE_PER_SQUARE_CENTIMETER` | Kilogram-Force per Square Centimeter | `kgf/cm^2` | 98066.5 | exact |
| `KILOGRAM_FORCE_PER_SQUARE_MILLIMETER` | Kilogram-Force per Square Millimeter | `kgf/mm^2` | 9806650 | exact |
| `GRAM_FORCE_PER_SQUARE_CENTIMETER` | Gram-Force per Square Centimeter | `gf/cm^2` | 98.0665 | exact |
| `SHORT_TON_FORCE_PER_SQUARE_FOOT` | Ton-Force (Short) per Square Foot | `tonf(short)/ft^2` | 95760.5179607 | exact |
| `SHORT_TON_FORCE_PER_SQUARE_INCH` | Ton-Force (Short) per Square Inch | `tonf(short)/in^2` | 13789514.5863 | exact |
| `LONG_TON_FORCE_PER_SQUARE_FOOT` | Ton-Force (Long) per Square Foot | `tonf(long)/ft^2` | 107251.780116 | exact |
| `LONG_TON_FORCE_PER_SQUARE_INCH` | Ton-Force (Long) per Square Inch | `tonf(long)/in^2` | 15444256.3367 | exact |
| `KIP_FORCE_PER_SQUARE_INCH` | Kip-Force per Square Inch | `kipf/in^2` | 6894757.29317 | exact |
| `POUND_FORCE_PER_SQUARE_FOOT` | Pound-Force per Square Foot | `lbf/ft^2` | 47.8802589803 | exact |
| `POUND_FORCE_PER_SQUARE_INCH` | Pound-Force per Square Inch | `lbf/in^2` | 6894.75729317 | exact |
| `POUNDAL_PER_SQUARE_FOOT` | Poundal per Square Foot | `pdl/ft^2` | 1.48816394357 | exact |
| `CENTIMETER_OF_MERCURY_0C` | Centimeter of Mercury (0°C) | `cmHg` | 1333.22 | convention |
| `MILLIMETER_OF_MERCURY_0C` | Millimeter of Mercury (0°C) | `mmHg` | 133.322 | convention |
| `INCH_OF_MERCURY_32F` | Inch of Mercury (32°F) | `inHg(32F)` | 3386.38 | convention |
| `INCH_OF_MERCURY_60F` | Inch of Mercury (60°F) | `inHg(60F)` | 3376.85 | convention |
| `CENTIMETER_OF_WATER_4C` | Centimeter of Water (4°C) | `cmH2O(4C)` | 98.0638 | convention |
| `MILLIMETER_OF_WATER_4C` | Millimeter of Water (4°C) | `mmH2O(4C)` | 9.80638 | convention |
| `INCH_OF_WATER_4C` | Inch of Water (4°C) | `inAq(4C)` | 249.082 | convention |
| `FOOT_OF_WATER_4C` | Foot of Water (4°C) | `ftAq(4C)` | 2988.98 | convention |
| `INCH_OF_WATER_60F` | Inch of Water (60°F) | `inAq(60F)` | 248.843 | convention |
| `FOOT_OF_WATER_60F` | Foot of Water (60°F) | `ftAq(60F)` | 2986.116 | convention |

## Energy

Enum `Unit.Energy`, base unit Joule (`J`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `GIGAJOULE` | Gigajoule | `GJ` | 1E9 | exact |
| `MEGAJOULE` | Megajoule | `MJ` | 1000000 | exact |
| `KILOJOULE` | Kilojoule | `kJ` | 1000 | exact |
| `JOULE` | Joule | `J` | 1 | exact |
| `MILLIJOULE` | Millijoule | `mJ` | 0.001 | exact |
| `MICROJOULE` | Microjoule | `µJ` | 0.000001 | exact |
| `NANOJOULE` | Nanojoule | `nJ` | 1E-9 | exact |
| `ATTOJOULE` | Attojoule | `aJ` | 1E-18 | exact |
| `GIGAWATT_HOUR` | Gigawatt-Hour | `GW*h` | 3.6E12 | exact |
| `MEGAWATT_HOUR` | Megawatt-Hour | `MW*h` | 3.6E9 | exact |
| `KILOWATT_HOUR` | Kilowatt-Hour | `kW*h` | 3600000 | exact |
| `WATT_HOUR` | Watt-Hour | `W*h` | 3600 | exact |
| `KILOWATT_SECOND` | Kilowatt-Second | `kW*s` | 1000 | exact |
| `WATT_SECOND` | Watt-Second | `W*s` | 1 | exact |
| `CALORIE_NUTRITIONAL` | Calorie (Nutritional) | `Cal` | 4186.8 | convention |
| `KILOCALORIE_IT` | Kilocalorie (IT) | `kcal(IT)` | 4186.8 | exact |
| `KILOCALORIE_TH` | Kilocalorie (th) | `kcal(th)` | 4184 | exact |
| `CALORIE_IT` | Calorie (IT) | `cal` | 4.1868 | exact |
| `CALORIE_TH` | Calorie (th) | `cal(th)` | 4.184 | exact |
| `BTU_IT` | Btu (IT) | `Btu` | 1055.05585262 | exact |
| `BTU_TH` | Btu (th) | `Btu(th)` | 1054.35026449 | exact |
| `MEGA_BTU_IT` | Mega Btu (IT) | `MBtu` | 1055055852.62 | exact |
| `THERM` | Therm | `therm` | 105505600 | convention |
| `THERM_EC` | Therm (EC) | `therm(EC)` | 105505600 | convention |
| `THERM_US` | Therm (US) | `therm(US)` | 105480400 | exact |
| `TON_HOUR_REFRIGERATION` | Ton-Hour (Refrigeration) | `ton_ref*h` | 12660670.2314 | exact |
| `HORSEPOWER_METRIC_HOUR` | Horsepower (Metric) Hour | `hp(metric)*h` | 2647795.5 | exact |
| `HORSEPOWER_HOUR` | Horsepower Hour | `hp*h` | 2684519.5377 | exact |
| `MEGAELECTRON_VOLT` | Megaelectron-Volt | `MeV` | 1.602176634E-13 | exact |
| `KILOELECTRON_VOLT` | Kiloelectron-Volt | `keV` | 1.602176634E-16 | exact |
| `ELECTRON_VOLT` | Electron-Volt | `eV` | 1.602176634E-19 | exact |
| `HARTREE_ENERGY` | Hartree Energy | `Eh` | 4.35974472221E-18 | measured |
| `RYDBERG_CONSTANT` | Rydberg Constant | `Ry` | 2.1798723611E-18 | measured |
| `ERG` | Erg | `erg` | 1E-7 | exact |
| `NEWTON_METER` | Newton Meter | `N*m` | 1 | exact |
| `DYNE_CENTIMETER` | Dyne Centimeter | `dyn*cm` | 1E-7 | exact |
| `GRAM_FORCE_METER` | Gram-Force Meter | `gf*m` | 0.00980665 | exact |
| `GRAM_FORCE_CENTIMETER` | Gram-Force Centimeter | `gf*cm` | 0.0000980665 | exact |
| `KILOGRAM_FORCE_CENTIMETER` | Kilogram-Force Centimeter | `kgf*cm` | 0.0980665 | exact |
| `KILOGRAM_FORCE_METER` | Kilogram-Force Meter | `kgf*m` | 9.80665 | exact |
| `KILOPOND_METER` | Kilopond Meter | `kp*m` | 9.80665 | exact |
| `POUND_FORCE_FOOT` | Pound-Force Foot | `lbf*ft` | 1.35581794833 | exact |
| `POUND_FORCE_INCH` | Pound-Force Inch | `lbf*in` | 0.112984829028 | exact |
| `OUNCE_FORCE_INCH` | Ounce-Force Inch | `ozf*in` | 0.00706155181423 | exact |
| `FOOT_POUND` | Foot-Pound | `ft*lbf` | 1.35581794833 | exact |
| `INCH_POUND` | Inch-Pound | `in*lbf` | 0.112984829028 | exact |
| `INCH_OUNCE` | Inch-Ounce | `in*ozf` | 0.00706155181423 | exact |
| `POUNDAL_FOOT` | Poundal Foot | `pdl*ft` | 0.0421401100938 | exact |
| `GIGATON_TNT` | Gigaton (TNT Equivalent) | `Gton` | 4.184E18 | exact |
| `MEGATON_TNT` | Megaton (TNT Equivalent) | `Mton` | 4.184E15 | exact |
| `KILOTON_TNT` | Kiloton (TNT Equivalent) | `kton` | 4.184E12 | exact |
| `TON_TNT` | Ton (Explosives) | `ton_TNT` | 4.184E9 | exact |
| `FUEL_OIL_EQUIVALENT_KILOLITER` | Fuel Oil Equivalent at Kiloliter | `foe@kL` | 40197627984.8 | convention |
| `FUEL_OIL_EQUIVALENT_US_BARREL` | Fuel Oil Equivalent at Barrel (United States) | `foe@bbl(US)` | 6383087908.35 | convention |

## Power

Enum `Unit.Power`, base unit Watt (`W`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `EXAWATT` | Exawatt | `EW` | 1E18 | exact |
| `PETAWATT` | Petawatt | `PW` | 1E15 | exact |
| `TERAWATT` | Terawatt | `TW` | 1E12 | exact |
| `GIGAWATT` | Gigawatt | `GW` | 1E9 | exact |
| `MEGAWATT` | Megawatt | `MW` | 1000000 | exact |
| `KILOWATT` | Kilowatt | `kW` | 1000 | exact |
| `HECTOWATT` | Hectowatt | `hW` | 100 | exact |
| `DEKAWATT` | Dekawatt | `daW` | 10 | exact |
| `WATT` | Watt | `W` | 1 | exact |
| `DECIWATT` | Deciwatt | `dW` | 0.1 | exact |
| `CENTIWATT` | Centiwatt | `cW` | 0.01 | exact |
| `MILLIWATT` | Milliwatt | `mW` | 0.001 | exact |
| `MICROWATT` | Microwatt | `µW` | 0.000001 | exact |
| `NANOWATT` | Nanowatt | `nW` | 1E-9 | exact |
| `PICOWATT` | Picowatt | `pW` | 1E-12 | exact |
| `FEMTOWATT` | Femtowatt | `fW` | 1E-15 | exact |
| `ATTOWATT` | Attowatt | `aW` | 1E-18 | exact |
| `HORSEPOWER` | Horsepower | `hp` | 745.699871582 | exact |
| `HORSEPOWER_MECHANICAL_550_FTLBF_PER_S` | Horsepower (550 ft*lbf/s) | `hp(550ft*lbf/s)` | 745.699871582 | exact |
| `HORSEPOWER_METRIC` | Horsepower (Metric) | `hp(metric)` | 735.49875 | exact |
| `HORSEPOWER_BOILER` | Horsepower (Boiler) | `hp(boiler)` | 9809.5 | convention |
| `HORSEPOWER_ELECTRIC` | Horsepower (Electric) | `hp(electric)` | 746 | exact |
| `HORSEPOWER_WATER` | Horsepower (Water) | `hp(water)` | 746.043 | convention |
| `PFERDESTAERKE` | Pferdestarke | `PS` | 735.49875 | exact |
| `BTU_IT_PER_HOUR` | Btu (IT) per Hour | `Btu/h` | 0.293071070172 | exact |
| `BTU_IT_PER_MINUTE` | Btu (IT) per Minute | `Btu/min` | 17.5842642103 | exact |
| `BTU_IT_PER_SECOND` | Btu (IT) per Second | `Btu/s` | 1055.05585262 | exact |
| `BTU_TH_PER_HOUR` | Btu (th) per Hour | `Btu(th)/h` | 0.292875073469 | exact |
| `BTU_TH_PER_MINUTE` | Btu (th) per Minute | `Btu(th)/min` | 17.5725044081 | exact |
| `BTU_TH_PER_SECOND` | Btu (th) per Second | `Btu(th)/s` | 1054.35026449 | exact |
| `MEGA_BTU_IT_PER_HOUR` | Mega Btu (IT) per Hour | `MBtu/h` | 293071.070172 | exact |
| `MBH` | MBH | `MBH` | 293.071070172 | exact |
| `TON_REFRIGERATION` | Ton (Refrigeration) | `TR` | 3516.85284207 | exact |
| `KILOCALORIE_IT_PER_HOUR` | Kilocalorie (IT) per Hour | `kcal(IT)/h` | 1.163 | exact |
| `KILOCALORIE_IT_PER_MINUTE` | Kilocalorie (IT) per Minute | `kcal(IT)/min` | 69.78 | exact |
| `KILOCALORIE_IT_PER_SECOND` | Kilocalorie (IT) per Second | `kcal(IT)/s` | 4186.8 | exact |
| `KILOCALORIE_TH_PER_HOUR` | Kilocalorie (th) per Hour | `kcal(th)/h` | 1.16222222222 | exact |
| `KILOCALORIE_TH_PER_MINUTE` | Kilocalorie (th) per Minute | `kcal(th)/min` | 69.7333333333 | exact |
| `KILOCALORIE_TH_PER_SECOND` | Kilocalorie (th) per Second | `kcal(th)/s` | 4184 | exact |
| `CALORIE_IT_PER_HOUR` | Calorie (IT) per Hour | `cal/h` | 0.001163 | exact |
| `CALORIE_IT_PER_MINUTE` | Calorie (IT) per Minute | `cal/min` | 0.06978 | exact |
| `CALORIE_IT_PER_SECOND` | Calorie (IT) per Second | `cal/s` | 4.1868 | exact |
| `CALORIE_TH_PER_HOUR` | Calorie (th) per Hour | `cal(th)/h` | 0.00116222222222 | exact |
| `CALORIE_TH_PER_MINUTE` | Calorie (th) per Minute | `cal(th)/min` | 0.0697333333333 | exact |
| `CALORIE_TH_PER_SECOND` | Calorie (th) per Second | `cal(th)/s` | 4.184 | exact |
| `FOOT_POUND_FORCE_PER_HOUR` | Foot Pound-Force per Hour | `ft*lbf/h` | 0.000376616096759 | exact |
| `FOOT_POUND_FORCE_PER_MINUTE` | Foot Pound-Force per Minute | `ft*lbf/min` | 0.0225969658055 | exact |
| `FOOT_POUND_FORCE_PER_SECOND` | Foot Pound-Force per Second | `ft*lbf/s` | 1.35581794833 | exact |
| `POUND_FOOT_PER_HOUR` | Pound-Foot per Hour | `lbf*ft/h` | 0.000376616096759 | exact |
| `POUND_FOOT_PER_MINUTE` | Pound-Foot per Minute | `lbf*ft/min` | 0.0225969658055 | exact |
| `POUND_FOOT_PER_SECOND` | Pound-Foot per Second | `lbf*ft/s` | 1.35581794833 | exact |
| `ERG_PER_SECOND` | Erg per Second | `erg/s` | 1E-7 | exact |
| `KILOVOLT_AMPERE` | Kilovolt-Ampere | `kV*A` | 1000 | exact |
| `VOLT_AMPERE` | Volt-Ampere | `V*A` | 1 | exact |
| `NEWTON_METER_PER_SECOND` | Newton Meter per Second | `N*m/s` | 1 | exact |
| `JOULE_PER_SECOND` | Joule per Second | `J/s` | 1 | exact |
| `EXAJOULE_PER_SECOND` | Exajoule per Second | `EJ/s` | 1E18 | exact |
| `PETAJOULE_PER_SECOND` | Petajoule per Second | `PJ/s` | 1E15 | exact |
| `TERAJOULE_PER_SECOND` | Terajoule per Second | `TJ/s` | 1E12 | exact |
| `GIGAJOULE_PER_SECOND` | Gigajoule per Second | `GJ/s` | 1E9 | exact |
| `MEGAJOULE_PER_SECOND` | Megajoule per Second | `MJ/s` | 1000000 | exact |
| `KILOJOULE_PER_SECOND` | Kilojoule per Second | `kJ/s` | 1000 | exact |
| `HECTOJOULE_PER_SECOND` | Hectojoule per Second | `hJ/s` | 100 | exact |
| `DEKAJOULE_PER_SECOND` | Dekajoule per Second | `daJ/s` | 10 | exact |
| `DECIJOULE_PER_SECOND` | Decijoule per Second | `dJ/s` | 0.1 | exact |
| `CENTIJOULE_PER_SECOND` | Centijoule per Second | `cJ/s` | 0.01 | exact |
| `MILLIJOULE_PER_SECOND` | Millijoule per Second | `mJ/s` | 0.001 | exact |
| `MICROJOULE_PER_SECOND` | Microjoule per Second | `µJ/s` | 0.000001 | exact |
| `NANOJOULE_PER_SECOND` | Nanojoule per Second | `nJ/s` | 1E-9 | exact |
| `PICOJOULE_PER_SECOND` | Picojoule per Second | `pJ/s` | 1E-12 | exact |
| `FEMTOJOULE_PER_SECOND` | Femtojoule per Second | `fJ/s` | 1E-15 | exact |
| `ATTOJOULE_PER_SECOND` | Attojoule per Second | `aJ/s` | 1E-18 | exact |
| `JOULE_PER_HOUR` | Joule per Hour | `J/h` | 0.000277777777778 | exact |
| `JOULE_PER_MINUTE` | Joule per Minute | `J/min` | 0.0166666666667 | exact |
| `KILOJOULE_PER_HOUR` | Kilojoule per Hour | `kJ/h` | 0.277777777778 | exact |
| `KILOJOULE_PER_MINUTE` | Kilojoule per Minute | `kJ/min` | 16.6666666667 | exact |

## Time

Enum `Unit.Time`, base unit Second (`s`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `MILLENNIUM` | Millennium | `millennium` | 3.15576E10 | exact |
| `CENTURY` | Century | `century` | 3.15576E9 | exact |
| `DECADE` | Decade | `decade` | 315576000 | exact |
| `YEAR` | Year | `y` | 31557600 | exact |
| `MONTH` | Month | `month` | 2628000 | convention |
| `WEEK` | Week | `week` | 604800 | exact |
| `DAY` | Day | `d` | 86400 | exact |
| `HOUR` | Hour | `h` | 3600 | exact |
| `MINUTE` | Minute | `min` | 60 | exact |
| `SECOND` | Second | `s` | 1 | exact |
| `MILLISECOND` | Millisecond | `ms` | 0.001 | exact |
| `MICROSECOND` | Microsecond | `µs` | 0.000001 | exact |
| `NANOSECOND` | Nanosecond | `ns` | 1E-9 | exact |
| `PICOSECOND` | Picosecond | `ps` | 1E-12 | exact |
| `FEMTOSECOND` | Femtosecond | `fs` | 1E-15 | exact |
| `ATTOSECOND` | Attosecond | `as` | 1E-18 | exact |
| `SHAKE` | Shake | `shake` | 1E-8 | exact |
| `MONTH_SYNODIC` | Month (Synodic) | `month (synodic)` | 2551443.84 | convention |
| `YEAR_JULIAN` | Year (Julian) | `year (Julian)` | 31557600 | exact |
| `YEAR_LEAP` | Year (Leap) | `year (leap)` | 31622400 | exact |
| `YEAR_TROPICAL` | Year (Tropical) | `year (tropical)` | 31556930 | convention |
| `YEAR_SIDEREAL` | Year (Sidereal) | `year (sidereal)` | 31558149.54 | convention |
| `DAY_SIDEREAL` | Day (Sidereal) | `day (sidereal)` | 86164.09 | convention |
| `HOUR_SIDEREAL` | Hour (Sidereal) | `hour (sidereal)` | 3590.17041667 | convention |
| `MINUTE_SIDEREAL` | Minute (Sidereal) | `minute (sidereal)` | 59.8361736111 | convention |
| `SECOND_SIDEREAL` | Second (Sidereal) | `second (sidereal)` | 0.9972695602 | convention |
| `FORTNIGHT` | Fortnight | `fortnight` | 1209600 | exact |
| `SEPTENNIAL` | Septennial | `septennial` | 220752000 | convention |
| `OCTENNIAL` | Octennial | `octennial` | 252288000 | convention |
| `NOVENNIAL` | Novennial | `novennial` | 283824000 | convention |
| `QUINDECENNIAL` | Quindecennial | `quindecennial` | 473040000 | convention |
| `QUINQUENNIAL` | Quinquennial | `quinquennial` | 157680000 | convention |
| `PLANCK_TIME` | Planck Time | `Planck time` | 5.391247E-44 | measured |

## Force

Enum `Unit.Force`, base unit Newton (`N`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `EXANEWTON` | Exanewton | `EN` | 1E18 | exact |
| `PETANEWTON` | Petanewton | `PN` | 1E15 | exact |
| `TERANEWTON` | Teranewton | `TN` | 1E12 | exact |
| `GIGANEWTON` | Giganewton | `GN` | 1E9 | exact |
| `MEGANEWTON` | Meganewton | `MN` | 1000000 | exact |
| `KILONEWTON` | Kilonewton | `kN` | 1000 | exact |
| `HECTONEWTON` | Hectonewton | `hN` | 100 | exact |
| `DEKANEWTON` | Dekanewton | `daN` | 10 | exact |
| `NEWTON` | Newton | `N` | 1 | exact |
| `DECINEWTON` | Decinewton | `dN` | 0.1 | exact |
| `CENTINEWTON` | Centinewton | `cN` | 0.01 | exact |
| `MILLINEWTON` | Millinewton | `mN` | 0.001 | exact |
| `MICRONEWTON` | Micronewton | `µN` | 0.000001 | exact |
| `NANONEWTON` | Nanonewton | `nN` | 1E-9 | exact |
| `PICONEWTON` | Piconewton | `pN` | 1E-12 | exact |
| `FEMTONEWTON` | Femtonewton | `fN` | 1E-15 | exact |
| `ATTONEWTON` | Attonewton | `aN` | 1E-18 | exact |
| `DYNE` | Dyne | `dyn` | 0.00001 | exact |
| `JOULE_PER_METER` | Joule per Meter | `J/m` | 1 | exact |
| `JOULE_PER_CENTIMETER` | Joule per Centimeter | `J/cm` | 0.01 | exact |
| `GRAM_FORCE` | Gram-Force | `gf` | 0.00980665 | exact |
| `KILOGRAM_FORCE` | Kilogram-Force | `kgf` | 9.80665 | exact |
| `TON_FORCE_METRIC` | Ton-Force (Metric) | `tf` | 9806.65 | exact |
| `TON_FORCE_SHORT` | Ton-Force (Short) | `ton-force (short)` | 8896.44323052 | exact |
| `TON_FORCE_LONG` | Ton-Force (Long) | `tonf (UK)` | 9964.01641818 | exact |
| `KIP_FORCE` | Kip-Force | `klbf` | 4448.22161526 | exact |
| `KILOPOUND_FORCE` | Kilopound-Force | `kipf` | 4448.22161526 | exact |
| `POUND_FORCE` | Pound-Force | `lbf` | 4.44822161526 | exact |
| `OUNCE_FORCE` | Ounce-Force | `ozf` | 0.278013850954 | exact |
| `POUNDAL` | Poundal | `pdl` | 0.138254954376 | exact |
| `POUND_FOOT_PER_SQUARE_SECOND` | Pound Foot per Square Second | `pound foot/square second` | 0.138254954376 | exact |
| `POND` | Pond | `p` | 0.00980665 | exact |
| `KILOPOND` | Kilopond | `kp` | 9.80665 | exact |

## Speed

Enum `Unit.Speed`, base unit Meter per Second (`m/s`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `KILOMETER_PER_HOUR` | Kilometer per Hour | `km/h` | 0.277777777778 | exact |
| `MILE_PER_HOUR` | Mile per Hour | `mi/h` | 0.44704 | exact |
| `METER_PER_HOUR` | Meter per Hour | `m/h` | 0.000277777777778 | exact |
| `METER_PER_MINUTE` | Meter per Minute | `m/min` | 0.0166666666667 | exact |
| `KILOMETER_PER_MINUTE` | Kilometer per Minute | `km/min` | 16.6666666667 | exact |
| `KILOMETER_PER_SECOND` | Kilometer per Second | `km/s` | 1000 | exact |
| `CENTIMETER_PER_HOUR` | Centimeter per Hour | `cm/h` | 0.00000277777777778 | exact |
| `CENTIMETER_PER_MINUTE` | Centimeter per Minute | `cm/min` | 0.000166666666667 | exact |
| `CENTIMETER_PER_SECOND` | Centimeter per Second | `cm/s` | 0.01 | exact |
| `MILLIMETER_PER_HOUR` | Millimeter per Hour | `mm/h` | 2.77777777778E-7 | exact |
| `MILLIMETER_PER_MINUTE` | Millimeter per Minute | `mm/min` | 0.0000166666666667 | exact |
| `MILLIMETER_PER_SECOND` | Millimeter per Second | `mm/s` | 0.001 | exact |
| `FOOT_PER_HOUR` | Foot per Hour | `ft/h` | 0.0000846666666667 | exact |
| `FOOT_PER_MINUTE` | Foot per Minute | `ft/min` | 0.00508 | exact |
| `FOOT_PER_SECOND` | Foot per Second | `ft/s` | 0.3048 | exact |
| `YARD_PER_HOUR` | Yard per Hour | `yd/h` | 0.000254 | exact |
| `YARD_PER_MINUTE` | Yard per Minute | `yd/min` | 0.01524 | exact |
| `YARD_PER_SECOND` | Yard per Second | `yd/s` | 0.9144 | exact |
| `MILE_PER_MINUTE` | Mile per Minute | `mi/min` | 26.8224 | exact |
| `MILE_PER_SECOND` | Mile per Second | `mi/s` | 1609.344 | exact |
| `KNOT` | Knot | `kn` | 0.514444444444 | exact |
| `KNOT_UK` | Knot (UK) | `kn_UK` | 0.514773333333 | exact |
| `SPEED_OF_LIGHT_VACUUM` | Velocity of Light in Vacuum | `c` | 299792458 | exact |
| `COSMIC_VELOCITY_FIRST` | Cosmic Velocity (First) | `v1` | 7900 | convention |
| `COSMIC_VELOCITY_SECOND` | Cosmic Velocity (Second) | `v2` | 11200 | convention |
| `COSMIC_VELOCITY_THIRD` | Cosmic Velocity (Third) | `v3` | 16670 | convention |
| `EARTHS_VELOCITY` | Earth's Velocity | `v_earth` | 29765 | convention |
| `SPEED_OF_SOUND_PURE_WATER` | Velocity of Sound in Pure Water | `v_sound_water` | 1482.7 | convention |
| `SPEED_OF_SOUND_SEA_WATER_20C_10M` | Velocity of Sound in Sea Water (20°C, 10 Meter Deep) | `v_sound_sea` | 1521.6 | convention |
| `MACH_20C_1ATM` | Mach (20°C, 1 atm) | `Ma(20°C)` | 343.6 | convention |
| `MACH_SI_STANDARD` | Mach (SI Standard) | `Ma(SI)` | 295.0464 | convention |
| `METER_PER_SECOND` | Meter per Second | `m/s` | 1 | exact |

## FuelConsumption

Enum `Unit.FuelConsumption`, base unit Meter per Liter (`m/L`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `EXAMETER_PER_LITER` | Exameter per Liter | `Em/L` | 1E18 | exact |
| `PETAMETER_PER_LITER` | Petameter per Liter | `Pm/L` | 1E15 | exact |
| `TERAMETER_PER_LITER` | Terameter per Liter | `Tm/L` | 1E12 | exact |
| `GIGAMETER_PER_LITER` | Gigameter per Liter | `Gm/L` | 1E9 | exact |
| `MEGAMETER_PER_LITER` | Megameter per Liter | `Mm/L` | 1000000 | exact |
| `KILOMETER_PER_LITER` | Kilometer per Liter | `km/L` | 1000 | exact |
| `HECTOMETER_PER_LITER` | Hectometer per Liter | `hm/L` | 100 | exact |
| `DEKAMETER_PER_LITER` | Dekameter per Liter | `dam/L` | 10 | exact |
| `CENTIMETER_PER_LITER` | Centimeter per Liter | `cm/L` | 0.01 | exact |
| `MILE_US_PER_LITER` | Mile (US) per Liter | `mi/L` | 1609.344 | exact |
| `NAUTICAL_MILE_PER_LITER` | Nautical Mile per Liter | `nmi/L` | 1852 | exact |
| `NAUTICAL_MILE_PER_GALLON_US` | Nautical Mile per Gallon (US) | `nmi/gal(US)` | 489.246640967 | exact |
| `KILOMETER_PER_GALLON_US` | Kilometer per Gallon (US) | `km/gal (US)` | 264.172052358 | exact |
| `METER_PER_GALLON_US` | Meter per Gallon (US) | `m/gal (US)` | 0.264172052358 | exact |
| `METER_PER_GALLON_UK` | Meter per Gallon (UK) | `m/gal (UK)` | 0.219969248299 | exact |
| `MILE_PER_GALLON_US` | Mile per Gallon (US) | `mi/gal (US)` | 425.14370743 | exact |
| `MILE_PER_GALLON_UK` | Mile per Gallon (UK) | `mi/gal (UK)` | 354.006189935 | exact |
| `METER_PER_CUBIC_METER` | Meter per Cubic Meter | `m/m^3` | 0.001 | exact |
| `METER_PER_CUBIC_CENTIMETER` | Meter per Cubic Centimeter | `m/cm^3` | 1000 | exact |
| `METER_PER_CUBIC_YARD` | Meter per Cubic Yard | `m/yd^3` | 0.00130795061931 | exact |
| `METER_PER_CUBIC_FOOT` | Meter per Cubic Foot | `m/ft^3` | 0.0353146667215 | exact |
| `METER_PER_CUBIC_INCH` | Meter per Cubic Inch | `m/in^3` | 61.0237440947 | exact |
| `METER_PER_QUART_US` | Meter per Quart (US) | `m/qt (US)` | 1.05668820943 | exact |
| `METER_PER_QUART_UK` | Meter per Quart (UK) | `m/qt (UK)` | 0.879876993196 | exact |
| `METER_PER_PINT_US` | Meter per Pint (US) | `m/pt (US)` | 2.11337641887 | exact |
| `METER_PER_PINT_UK` | Meter per Pint (UK) | `m/pt (UK)` | 1.75975398639 | exact |
| `METER_PER_CUP_US` | Meter per Cup (US) | `m/cup (US)` | 4.22675283773 | exact |
| `METER_PER_CUP_UK` | Meter per Cup (UK) | `m/cup (UK)` | 3.51950797279 | exact |
| `METER_PER_FLUID_OUNCE_US` | Meter per Fluid Ounce (US) | `m/fl oz (US)` | 33.8140227018 | exact |
| `METER_PER_FLUID_OUNCE_UK` | Meter per Fluid Ounce (UK) | `m/fl oz (UK)` | 35.1950797279 | exact |
| `LITER_PER_METER` | Liter per Meter | `L/m` | 1 | exact |
| `LITER_PER_100_KILOMETER` | Liter per 100 Kilometer | `L/100 km` | 100000 | exact |
| `GALLON_US_PER_MILE` | Gallon (US) per Mile | `gal (US)/mi` | 425.14370743 | exact |
| `GALLON_US_PER_100_MILES` | Gallon (US) per 100 Miles | `gal (US)/100 mi` | 42514.370743 | exact |
| `GALLON_UK_PER_MILE` | Gallon (UK) per Mile | `gal (UK)/mi` | 354.006189935 | exact |
| `GALLON_UK_PER_100_MILES` | Gallon (UK) per 100 Miles | `gal (UK)/100 mi` | 35400.6189935 | exact |
| `METER_PER_LITER` | Meter per Liter | `m/L` | 1 | exact |

## DataStorage

Enum `Unit.DataStorage`, base unit Bit (`bit`).

| Constant | Name | Symbol | 1 unit in base | Basis |
| --- | --- | --- | ---: | --- |
| `BIT` | Bit | `bit` | 1 | exact |
| `NIBBLE` | Nibble | `nibble` | 4 | exact |
| `BYTE` | Byte | `B` | 8 | exact |
| `CHARACTER` | Character | `char` | 8 | exact |
| `WORD` | Word | `word` | 16 | convention |
| `MAPM_WORD` | MAPM-Word | `MAPM-word` | 32 | convention |
| `QUADRUPLE_WORD` | Quadruple-Word | `quadruple-word` | 64 | convention |
| `BLOCK` | Block | `block` | 4096 | convention |
| `KIBIBIT` | Kibibit | `Kibit` | 1024 | exact |
| `KIBIBYTE` | Kibibyte | `KiB` | 8192 | exact |
| `MEBIBIT` | Mebibit | `Mibit` | 1048576 | exact |
| `MEBIBYTE` | Mebibyte | `MiB` | 8388608 | exact |
| `GIBIBIT` | Gibibit | `Gibit` | 1073741824 | exact |
| `GIBIBYTE` | Gibibyte | `GiB` | 8589934592 | exact |
| `TEBIBIT` | Tebibit | `Tibit` | 1.09951162778E12 | exact |
| `TEBIBYTE` | Tebibyte | `TiB` | 8.79609302221E12 | exact |
| `PEBIBIT` | Pebibit | `Pibit` | 1.12589990684E15 | exact |
| `PEBIBYTE` | Pebibyte | `PiB` | 9.00719925474E15 | exact |
| `EXBIBIT` | Exbibit | `Eibit` | 1.15292150461E18 | exact |
| `EXBIBYTE` | Exbibyte | `EiB` | 9.22337203685E18 | exact |
| `KILOBYTE_DECIMAL` | Kilobyte (10^3 bytes) | `kB` | 8000 | exact |
| `MEGABYTE_DECIMAL` | Megabyte (10^6 bytes) | `MB` | 8000000 | exact |
| `GIGABYTE_DECIMAL` | Gigabyte (10^9 bytes) | `GB` | 8E9 | exact |
| `TERABYTE_DECIMAL` | Terabyte (10^12 bytes) | `TB` | 8E12 | exact |
| `PETABYTE_DECIMAL` | Petabyte (10^15 bytes) | `PB` | 8E15 | exact |
| `EXABYTE_DECIMAL` | Exabyte (10^18 bytes) | `EB` | 8E18 | exact |
| `FLOPPY_35_DD` | Floppy Disk (3.5", DD) | `floppy_3.5_DD` | 5830656 | convention |
| `FLOPPY_35_HD` | Floppy Disk (3.5", HD) | `floppy_3.5_HD` | 11661312 | convention |
| `FLOPPY_35_ED` | Floppy Disk (3.5", ED) | `floppy_3.5_ED` | 23322624 | convention |
| `FLOPPY_525_DD` | Floppy Disk (5.25", DD) | `floppy_5.25_DD` | 2915328 | convention |
| `FLOPPY_525_HD` | Floppy Disk (5.25", HD) | `floppy_5.25_HD` | 9711616 | convention |
| `ZIP_100` | Zip 100 | `zip_100` | 803454976 | convention |
| `ZIP_250` | Zip 250 | `zip_250` | 2.00863744E9 | convention |
| `JAZ_1GB` | Jaz 1GB | `jaz_1GB` | 8589934592 | convention |
| `JAZ_2GB` | Jaz 2GB | `jaz_2GB` | 17179869184 | convention |
| `CD_74_MIN` | CD (74 minute) | `cd_74_min` | 5448466432 | convention |
| `CD_80_MIN` | CD (80 minute) | `cd_80_min` | 5890233976 | convention |
| `DVD_1L_1S` | DVD (1 layer, 1 side) | `dvd_1L_1S` | 40372692582.4 | convention |
| `DVD_2L_1S` | DVD (2 layer, 1 side) | `dvd_2L_1S` | 73014444032 | convention |
| `DVD_1L_2S` | DVD (1 layer, 2 side) | `dvd_1L_2S` | 80745385164.8 | convention |
| `DVD_2L_2S` | DVD (2 layer, 2 side) | `dvd_2L_2S` | 146028888064 | convention |
