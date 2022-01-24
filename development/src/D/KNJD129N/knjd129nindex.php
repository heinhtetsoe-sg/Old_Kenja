<?php

require_once('for_php7.php');


require_once('knjd129nModel.inc');
require_once('knjd129nQuery.inc');

class knjd129nController extends Controller {
    var $ModelClassName = "knjd129nModel";
    var $ProgramID      = "KNJD129N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "disp":
                case "subclasscd":
                    $this->callView("knjd129nForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd129nForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd129nForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd129nCtl = new knjd129nController;
?>
