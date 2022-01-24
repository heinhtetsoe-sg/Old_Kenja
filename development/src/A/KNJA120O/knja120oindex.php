<?php

require_once('for_php7.php');

require_once('knja120oModel.inc');
require_once('knja120oQuery.inc');

class knja120oController extends Controller {
    var $ModelClassName = "knja120oModel";
    var $ProgramID      = "KNJA120O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja120oForm1");
                    break 2;
                case "subform1": //通知表所見参照
                    $this->callView("knja120oSubForm1");
                    break 2;
                case "subform2": //部活動参照
                    $this->callView("knja120oSubForm2");
                    break 2;
                case "subform3": //委員会参照
                    $this->callView("knja120oSubForm3");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == ""){
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA120O/knja120oindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode($programpath."/knja120oindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knja120oindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knja120oCtl = new knja120oController;
?>
