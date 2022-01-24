<?php

require_once('for_php7.php');


require_once('knjd143aModel.inc');
require_once('knjd143aQuery.inc');

class knjd143aController extends Controller {
    var $ModelClassName = "knjd143aModel";
    var $ProgramID      = "KNJD143A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "schoolkind";
                case "subclasscd":
                case "grade_hr_class";
                case "reset":
                case "back":
                case "semester":
                    $this->callView("knjd143aForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
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
$knjd143aCtl = new knjd143aController;
?>
