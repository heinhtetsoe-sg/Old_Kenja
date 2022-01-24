<?php

require_once('for_php7.php');
require_once('knjd132iModel.inc');
require_once('knjd132iQuery.inc');

class knjd132iController extends Controller {
    var $ModelClassName = "knjd132iModel";
    var $ProgramID      = "KNJD132I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjd132iForm1");
                    break 2;
                case "subform2": //部活動参照
                    $this->callView("knjd132iSubForm2");
                    break 2;
                case "subform3": //委員会参照
                    $this->callView("knjd132iSubForm3");
                    break 2;
                case "subform6": //記録備考参照
                    $this->callView("knjd132iSubForm6");
                    break 2;
                case "subform5": //検定選択
                    $this->callView("knjd132iSubForm5");
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
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132I/knjd132iindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode($programpath."/knjd132iindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knjd132iindex.php?cmd=edit";
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
$knjd132iCtl = new knjd132iController;
?>
