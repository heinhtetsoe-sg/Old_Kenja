<?php

require_once('for_php7.php');

require_once('knja121cModel.inc');
require_once('knja121cQuery.inc');

class knja121cController extends Controller {
    var $ModelClassName = "knja121cModel";
    var $ProgramID      = "KNJA121C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "value_set":
                case "edit":
                case "clear":
                    $this->callView("knja121cForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "form2":
                case "clear2":
                    $this->callView("knja121cForm2");
                    break 2;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "subform1":    //通知表所見参照
                    $this->callView("knja121cSubForm1");
                    break 2;
                case "subform2":    //出欠の記録参照
                    $this->callView("knja121cSubForm2");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "shokenlist1":
                case "shokenlist2":
                case "shokenlist3":
                    $this->callView("shokenlist");
                    break 2;
                case "teikei_act":
                case "teikei_val":
                    $this->callView("knja121cSubMaster");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA121C/knja121cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["right_src"] = "knja121cindex.php?cmd=edit";
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
$knja121cCtl = new knja121cController;
?>
