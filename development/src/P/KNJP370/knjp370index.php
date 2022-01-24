<?php

require_once('for_php7.php');

require_once('knjp370Model.inc');
require_once('knjp370Query.inc');

class knjp370Controller extends Controller {
    var $ModelClassName = "knjp370Model";
    var $ProgramID      = "knjp370";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp370":                             //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //メニュー画面もしくはSUBMITした場合
                case "read":                                //NO001
                    $sessionInstance->knjp370Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp370Form1");
                    exit;
                //NO001
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp370Ctl = new knjp370Controller;
//var_dump($_REQUEST);
?>
