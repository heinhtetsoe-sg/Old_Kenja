<?php

require_once('for_php7.php');
require_once('knja121kModel.inc');
require_once('knja121kQuery.inc');

class knja121kController extends Controller {
    var $ModelClassName = "knja121kModel";
    var $ProgramID      = "KNJA121K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja121kForm1");
                    break 2;
                case "subform1": //通知表所見参照
                    $this->callView("knja121kSubForm1");
                    break 2;
                case "form2": //記録
                    $this->callView("knja121kForm2");
                    break 2;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA121K/knja121kindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["right_src"] = "knja121kindex.php?cmd=edit";
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
$knja121kCtl = new knja121kController;
?>
