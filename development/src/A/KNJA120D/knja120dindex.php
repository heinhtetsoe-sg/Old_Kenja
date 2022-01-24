<?php

require_once('for_php7.php');
require_once('knja120dModel.inc');
require_once('knja120dQuery.inc');

class knja120dController extends Controller {
    var $ModelClassName = "knja120dModel";
    var $ProgramID      = "KNJA120D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "value_set":
                case "torikomi3":
                case "reload":
                case "reload2":
                case "edit":
                case "clear":
                    $this->callView("knja120dForm1");
                    break 2;
                case "subform1": //通知表所見参照
                    $this->callView("knja120dSubForm1");
                    break 2;
                case "subform4": //成績
                    $this->callView("knja120dSubForm4");
                    break 2;
                case "act_doc":  //行動の記録参照
                    $this->callView("knja120dActDoc");
                    break 2;
                case "teikei_act":
                case "teikei_val":
                    $this->callView("knja120dSubMaster");
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
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA120D/knja120dindex.php?cmd=edit") ."&button=1&SCHOOL_KIND=H";
                    $args["right_src"] = "knja120dindex.php?cmd=edit";
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
$knja120dCtl = new knja120dController;
?>
