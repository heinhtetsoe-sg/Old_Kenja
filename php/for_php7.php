<?php

require_once('for_php7.php');


require_once('for_php7.php');

//php7.3.4バージョンアップ用オリジナル関数

/*このファイルの呼び出し（絶対パス）
    require_once("/usr/local/lib/php/for_php7.php");
*/

function get_count($var)
{
    if($var === NULL)
    {
        return 0;
    }
    else
    {
        return ((is_array($var) || $var instanceof countable) ? count($var) : 1 );
    }
}


