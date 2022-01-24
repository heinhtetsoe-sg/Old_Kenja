<?php

require_once('for_php7.php');
require_once('knjd139pModel.inc');
require_once('knjd139pQuery.inc');

class knjd139pController extends Controller {
    var $ModelClassName = "knjd139pModel";
    var $ProgramID      = "KNJD139P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd139pForm1");
                    break 2;
                case "subform1": //係り参照
                    $this->callView("knjd139pSubForm1");
                    break 2;
                case "subform2": //委員会参照
                    $this->callView("knjd139pSubForm2");
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
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD139P/knjd139pindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["right_src"] = "knjd139pindex.php?cmd=edit";
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
$knjd139pCtl = new knjd139pController;
?>
