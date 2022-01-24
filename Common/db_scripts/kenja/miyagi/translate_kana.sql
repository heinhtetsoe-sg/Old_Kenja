drop function TRANSLATE_KANA

CREATE FUNCTION TRANSLATE_KANA \
(   henkanStr       varchar(240) \
) RETURNS varchar(240) \
 CONTAINS SQL \
 SPECIFIC TRANSLATE_KANA \
 LANGUAGE SQL \ 
 NO EXTERNAL ACTION \
 DETERMINISTIC \
  \
  RETURN (VALUES(translate(translate(henkanStr, \
                       'かきくけこさしすせそたちつてとはひふへほはひふへほ', \
                       'がぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽ'), \
                       'カキクケコサシスセソタチステトハヒフヘホハヒフヘホ', \
                       'ガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポ')) \
               )
