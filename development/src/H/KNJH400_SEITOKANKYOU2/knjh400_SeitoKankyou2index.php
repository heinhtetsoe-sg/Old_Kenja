<?php
require_once('knjh400_SeitoKankyou2Model.inc');
require_once('knjh400_SeitoKankyou2Query.inc');

require_once('for_php7.php');

class knjh400_SeitoKankyou2Controller extends Controller
{
    public $ModelClassName = "knjh400_SeitoKankyou2Model";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_SeitoKankyou2Form1");
                    break 2;
                case "subform1": //通知表所見参照
                case "reset2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_SeitoKankyou2Form1");
                    break 2;
                case "subform2":
                case "get_rosen":
                case "get_rosen_from_keiro":
                case "get_station":
                case "get_station_from_keiro":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_SeitoKankyou2Form1");
                    break 2;
                case "subform3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_SeitoKankyou2SubForm3");
                    break 2;
                case "reset":
                    $this->callView("knjh400_SeitoKankyou2Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "back2":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP3/index.php?PROGRAMID=KNJH160A&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH010A/knjh400_SeitoKankyou2index.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjh400_SeitoKankyou2index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    View::frame($args);
                    break 2;
                case "":
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH010A/knjh400_SeitoKankyou2index.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjh400_SeitoKankyou2index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
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
$knjh400_SeitoKankyou2Ctl = new knjh400_SeitoKankyou2Controller();
