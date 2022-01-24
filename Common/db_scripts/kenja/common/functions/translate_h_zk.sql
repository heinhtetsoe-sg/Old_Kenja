drop function TRANSLATE_H_ZK 
CREATE FUNCTION TRANSLATE_H_ZK 
(   HENKANSTR varchar(240) 
) RETURNS varchar(240) 
 CONTAINS SQL 
 SPECIFIC TRANSLATE_H_ZK  
 LANGUAGE SQL 
 NO EXTERNAL ACTION 
 DETERMINISTIC 
        RETURN (VALUES(translate(henkanStr, 
                             'アアイイウウエエオオカカキキククケケココササシシススセセソソタタチチツツツテテトトナニヌネノハハハヒヒヒフフフヘヘヘホホホマミムメモヤヤユユヨヨラリルレロヮワヰヱヲン', 
                             'ぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをん')))
