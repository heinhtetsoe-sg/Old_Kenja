<?php

require_once('for_php7.php');

require_once('knjg020aModel.inc');
require_once('knjg020aQuery.inc');

class knjg020aController extends Controller {
    var $ModelClassName = "knjg020aModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg020a":
                case "updEdit":
                    $sessionInstance->knjg020aModel();
                    $this->callView("knjg020aForm1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjg020aCtl = new knjg020aController;
//var_dump($_REQUEST);
?>
