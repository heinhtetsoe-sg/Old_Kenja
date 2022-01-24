<?php

require_once('for_php7.php');

require_once('knjmp920_meisaiModel.inc');
require_once('knjmp920_meisaiQuery.inc');

class knjmp920_meisaiController extends Controller {
    var $ModelClassName = "knjmp920_meisaiModel";
    var $ProgramID      = "KNJMP920_MEISAI";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjmp920_meisai":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp920_meisaiModel();        //コントロールマスタの呼び出し
                    $this->callView("knjmp920_meisaiForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjmp920_meisaiCtl = new knjmp920_meisaiController;
//var_dump($_REQUEST);
?>

