<?php
require_once('knje063dModel.inc');
require_once('knje063dQuery.inc');

class knje063dController extends Controller
{
    public $ModelClassName = "knje063dModel";
    public $ProgramID      = "KNJE063D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "add_year":
                case "change_year":
                case "class":
                case "subclass":
                case "updEdit":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje063dForm1");
                    break 2;
                case "delete":
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje063dForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "reset":
                    $this->callView("knje063dForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE063D/knje063dindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje063dindex.php?cmd=edit&init=1";
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
$knje063dCtl = new knje063dController();
//var_dump($_REQUEST);
?>
