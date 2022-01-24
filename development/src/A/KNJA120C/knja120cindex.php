<?php

require_once('for_php7.php');

require_once('knja120cModel.inc');
require_once('knja120cQuery.inc');

class knja120cController extends Controller {
    var $ModelClassName = "knja120cModel";
    var $ProgramID      = "KNJA120C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja120cForm1");
                    break 2;
                case "subform1": //通知表所見参照
                    $this->callView("knja120cSubForm1");
                    break 2;
                case "subform2": //部活動参照
                    $this->callView("knja120cSubForm2");
                    break 2;
                case "subform3": //委員会参照
                    $this->callView("knja120cSubForm3");
                    break 2;
                case "subform4": //成績
                    $this->callView("knja120cSubForm4");
                    break 2;
                case "subform5": //資格
                    $this->callView("knja120cSubForm5");
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
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA120C/knja120cindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja120cindex.php?cmd=edit";
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
$knja120cCtl = new knja120cController;
?>
