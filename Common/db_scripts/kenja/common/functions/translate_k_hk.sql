drop function TRANSLATE_K_HK

CREATE FUNCTION TRANSLATE_K_HK \
(   henkanStr varchar(240) \
) RETURNS varchar(240) \
 CONTAINS SQL \
 SPECIFIC TRANSLATE_K_HK \
 LANGUAGE SQL \ 
 NO EXTERNAL ACTION \
 DETERMINISTIC \
        RETURN (VALUES(translate(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(henkanStr, 'ポ', 'ﾎﾟ'), 'ボ', 'ﾎﾞ'), 'ペ', 'ﾍﾟ'), 'ベ', 'ﾍﾞ'), 'プ', 'ﾌﾟ'), 'ブ', 'ﾌﾞ'), 'ピ', 'ﾋﾟ'), 'ビ', 'ﾋﾞ'), 'パ', 'ﾊﾟ'), 'バ', 'ﾊﾞ'), 'ド', 'ﾄﾞ'), 'デ', 'ﾃﾞ'), 'ヅ', 'ﾂﾞ'), 'ヂ', 'ﾁﾞ'), 'ダ', 'ﾀﾞ'), 'ゾ', 'ｿﾞ'), 'ゼ', 'ｾﾞ'), 'ズ', 'ｽﾞ'), 'ジ', 'ｼﾞ'), 'ザ', 'ｻﾞ'), 'ゴ', 'ｺﾞ'), 'ゲ', 'ｹﾞ'), 'グ', 'ｸﾞ'), 'ギ', 'ｷﾞ'), 'ガ', 'ｶﾞ'), 'ヴ', 'ｳﾞ'), \
                             'ｧｱｨｲｩｳｪｴｫｵｶｷｸｹｺｻｼｽｾｿﾀﾁｯﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓｬﾔｭﾕｮﾖﾗﾘﾙﾚﾛヮﾜｦﾝ', \
                             'ァアィイゥウェエォオカキクケコサシスセソタチッツテトナニヌネノハヒフヘホマミムメモャヤュユョヨラリルレロヮワヲン')))
