<?php

require_once('for_php7.php');

require_once('knjmp910_schregModel.inc');
require_once('knjmp910_schregQuery.inc');

class knjmp910_schregController extends Controller {
    var $ModelClassName = "knjmp910_schregModel";
    var $ProgramID      = "KNJMP910_SCHREG";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "knjmp910_schreg":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp910_schregModel();        //コントロールマスタの呼び出し
                    $this->callView("knjmp910_schregForm1");
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
$knjmp910_schregCtl = new knjmp910_schregController;
//var_dump($_REQUEST);
?>

