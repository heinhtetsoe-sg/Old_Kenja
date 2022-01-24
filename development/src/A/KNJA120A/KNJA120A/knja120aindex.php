<?php

require_once('for_php7.php');

require_once('knja120aModel.inc');
require_once('knja120aQuery.inc');

class knja120aController extends Controller {
    var $ModelClassName = "knja120aModel";
    var $ProgramID      = "KNJA120A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi3":
                case "torikomi4":
                case "reload":
                case "reload2":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knja120aForm1");
                    break 2;
                case "subform1":    //通知表所見参照
                    $this->callView("knja120aSubForm1");
                    break 2;
                case "subform4":    //成績
                    $this->callView("knja120aSubForm4");
                    break 2;
                case "syukketsu":   //出欠の記録参照
                    $this->callView("knja120aSyukketsuKirokuSansyo");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA120A/knja120aindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knja120aindex.php?cmd=edit&init=1";
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
$knja120aCtl = new knja120aController;
?>
