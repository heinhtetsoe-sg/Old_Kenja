<?php

require_once('for_php7.php');

require_once('knjh400_assessModel.inc');
require_once('knjh400_assessQuery.inc');

class knjh400_assessController extends Controller {
    var $ModelClassName = "knjh400_assessModel";
    var $ProgramID      = "KNJh400_assess";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_assessSubForm1");
                    break 2;
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_assessSubForm1");
                    break 2;
                case "subform2":
                case "subform2A":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_assessSubForm2");
                    break 2;
                //障害マスタ参照
                case "reference1":
                    $this->callView("knjh400_assessSubRef1");
                    break 2;
                case "subform1_update":
                case "subform2_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GUI/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJh400_assess/knjh400_assessindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjh400_assessindex.php?cmd=edit";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_assessCtl = new knjh400_assessController;
?>
