<?php

require_once('for_php7.php');
require_once('knjd130hModel.inc');
require_once('knjd130hQuery.inc');

class knjd130hController extends Controller {
    var $ModelClassName = "knjd130hModel";
    var $ProgramID      = "KNJD130H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd130hForm1");
                    break 2;
                case "subform1": //成績参照
                    $this->callView("knjd130hSubForm1");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD130H/knjd130hindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjd130hindex.php?cmd=edit";
                    $args["cols"] = "30%,*";
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
$knjd130hCtl = new knjd130hController;
?>
