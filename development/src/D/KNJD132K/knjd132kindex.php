<?php

require_once('for_php7.php');

require_once('knjd132kModel.inc');
require_once('knjd132kQuery.inc');

class knjd132kController extends Controller
{
    public $ModelClassName = "knjd132kModel";
    public $ProgramID      = "KNJD132K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "clear":
                    $this->callView("knjd132kForm1");
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
                    $args["left_src"]   = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132K/knjd132kindex.php?cmd=edit") ."&button=1&SCHOOL_KIND=J";
                    $args["right_src"]  = "knjd132kindex.php?cmd=edit";
                    $args["cols"] = "25%,*";
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
$knjd132kCtl = new knjd132kController();
