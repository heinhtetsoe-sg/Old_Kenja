<?php

require_once('for_php7.php');

require_once('knja120Model.inc');
require_once('knja120Query.inc');

class knja120Controller extends Controller {
    var $ModelClassName = "knja120Model";
    var $ProgramID      = "KNJA120";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi3":
                case "chousasho":
                case "daigae":
                case "reload":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knja120Form1");
                    break 2;
                case "subform1":        //通知表所見参照
                    $this->callView("knja120SubForm1");
                    break 2;
                case "subform4":        //成績
                    $this->callView("knja120SubForm4");
                    break 2;
                case "syukketsu":       //出欠の記録参照
                    $this->callView("syukketsuKirokuSansyo");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == ""){
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/A/KNJA120/knja120index.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode($programpath."/knja120index.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knja120index.php?cmd=edit";
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
$knja120Ctl = new knja120Controller;
?>
