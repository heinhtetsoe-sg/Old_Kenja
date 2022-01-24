<?php

require_once('for_php7.php');

require_once('knja126jModel.inc');
require_once('knja126jQuery.inc');

class knja126jController extends Controller {
    var $ModelClassName = "knja126jModel";
    var $ProgramID      = "KNJA126J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja126jForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "form2":
                case "clear2":
                    $this->callView("knja126jForm2");
                    break 2;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "subform1":    //通知表所見参照
                    $this->callView("knja126jSubForm1");
                    break 2;
                case "subform2":    //出欠の記録参照
                    $this->callView("knja126jSubForm2");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knja126jForm");
                    break 2;
                case "shokenlist1":
                case "shokenlist2":
                case "shokenlist3":
                    $this->callView("shokenlist");
                    break 2;
                case "main":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA126J/knja126jindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["right_src"] = "knja126jindex.php?cmd=edit";
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
$knja126jCtl = new knja126jController;
?>
