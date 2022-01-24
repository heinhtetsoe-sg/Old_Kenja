<?php

require_once('for_php7.php');

require_once('knjl362Model.inc');
require_once('knjl362Query.inc');

class knjl362Controller extends Controller {
    var $ModelClassName = "knjl362Model";
    var $ProgramID      = "KNJL362";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl362":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl362Model();        //コントロールマスタの呼び出し
                    $this->callView("knjl362Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl362Ctl = new knjl362Controller;
//var_dump($_REQUEST);
?>
