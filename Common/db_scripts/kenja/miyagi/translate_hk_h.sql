drop function TRANSLATE_HK_H

CREATE FUNCTION TRANSLATE_HK_H \
(   henkanStr varchar(240) \
) RETURNS varchar(240) \
 CONTAINS SQL \
 SPECIFIC TRANSLATE_HK_H \
 LANGUAGE SQL \ 
 NO EXTERNAL ACTION \
 DETERMINISTIC \
        RETURN (VALUES(translate(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(henkanStr, 'ｳﾞ', 'う'), 'ﾎﾟ', 'ぽ'), 'ﾎﾞ', 'ぼ'), 'ﾍﾟ', 'ぺ'), 'ﾍﾞ', 'べ'), 'ﾌﾟ', 'ぷ'), 'ﾌﾞ', 'ぶ'), 'ﾋﾟ', 'ぴ'), 'ﾋﾞ', 'び'), 'ﾊﾟ', 'ぱ'), 'ﾊﾞ', 'ば'), 'ﾄﾞ', 'ど'), 'ﾃﾞ', 'で'), 'ﾂﾞ', 'づ'), 'ﾁﾞ', 'ぢ'), 'ﾀﾞ', 'だ'), 'ｿﾞ', 'ぞ'), 'ｾﾞ', 'ぜ'), 'ｽﾞ', 'ず'), 'ｼﾞ', 'じ'), 'ｻﾞ', 'ざ'), 'ｺﾞ', 'ご'), 'ｹﾞ', 'げ'), 'ｸﾞ', 'ぐ'), 'ｷﾞ', 'ぎ'), 'ｶﾞ', 'が'), \
                             'ぁあぃいぅうぇえぉおかきくけこさしすせそたちっつてとなにぬねのはひふへほまみむめもゃやゅゆょよらりるれろゎわをん', \
                             'ｧｱｨｲｩｳｪｴｫｵｶｷｸｹｺｻｼｽｾｿﾀﾁｯﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓｬﾔｭﾕｮﾖﾗﾘﾙﾚﾛヮﾜｦﾝ')))
