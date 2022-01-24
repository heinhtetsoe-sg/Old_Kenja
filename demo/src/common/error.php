<?php

require_once('for_php7.php');

//デバッグ用
function print_r_log($var)
{
    ob_start();
    print_r($var);
    $ret_str = ob_get_contents();
    ob_end_clean();
    
    return $ret_str;
}
//デバッグ用
if( is_object($model->error) ){
    die(print_r_log($model));
}
?>