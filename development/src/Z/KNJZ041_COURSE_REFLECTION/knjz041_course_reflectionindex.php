<?php

require_once('for_php7.php');

require_once('knjz041_course_reflectionModel.inc');
require_once('knjz041_course_reflectionQuery.inc');

class knjz041_course_reflectionController extends Controller {
    var $ModelClassName = "knjz041_course_reflectionModel";
    var $ProgramID      = "KNJZ041_COURSE_REFLECTION";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz041_course_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz041_course_reflectionCtl = new knjz041_course_reflectionController;
?>
