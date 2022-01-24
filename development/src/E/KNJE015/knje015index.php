<?php

require_once('for_php7.php');

require_once('knje015Model.inc');
require_once('knje015Query.inc');

class knje015Controller extends Controller
{
    public $ModelClassName = "knje015Model";
    public $ProgramID      = "KNJE015";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                    $this->callView("knje015Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje015Form1", $sessionInstance->auth);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "tuutihyou":
                    $this->callView("tuutihyou");
                    break 2;
                case "bukatu":
                    $this->callView("bukatu");
                    break 2;
                case "sikaku":
                    $this->callView("sikaku");
                    break 2;
                case "reset":
                    $this->callView("knje015Form1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE015/knje015index.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje015index.php?cmd=edit&init=1";
                    $args["cols"] = "22%,*";
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
$knje015Ctl = new knje015Controller();
//var_dump($_REQUEST);
