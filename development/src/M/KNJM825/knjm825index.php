<?php

require_once('for_php7.php');

require_once('knjm825Model.inc');
require_once('knjm825Query.inc');

class knjm825Controller extends Controller {
    var $ModelClassName = "knjm825Model";
    var $ProgramID      = "KNJM825";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm825":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm825Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm825Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm825Ctl = new knjm825Controller;
//var_dump($_REQUEST);
?>

