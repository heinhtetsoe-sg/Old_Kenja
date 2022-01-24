<?php

require_once('for_php7.php');
require_once('knjd635Model.inc');
require_once('knjd635Query.inc');

class knjd635Controller extends Controller {
    var $ModelClassName = "knjd635Model";
    var $ProgramID      = "KNJD635";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "clear":
                    $this->callView("knjd635Form1");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD635/knjd635index.php?cmd=edit") ."&button=1" ."&SES_FLG=1";
                    $args["right_src"] = "knjd635index.php?cmd=edit";
                    $args["cols"] = "35%,*";
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
$knjd635Ctl = new knjd635Controller;
?>
