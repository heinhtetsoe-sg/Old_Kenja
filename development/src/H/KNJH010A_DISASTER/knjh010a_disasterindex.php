<?php

require_once('for_php7.php');

require_once('knjh010a_disasterModel.inc');
require_once('knjh010a_disasterQuery.inc');

class knjh010a_disasterController extends Controller
{
    public $ModelClassName = "knjh010a_disasterModel";
    public $ProgramID      = "KNJH010A_DISASTER";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh010a_disasterForm1");
                    break 2;
                case "subform2":
                case "get_rosen":
                case "get_rosen_from_keiro":
                case "get_station":
                case "get_station_from_keiro":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh010a_disasterSubForm2");
                    break 2;
                case "subform3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh010a_disasterSubForm3");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    if (!$sessionInstance->auth) {
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $this->callView("knjh010a_disasterForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "back2":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP3/index.php?PROGRAMID=KNJH160A&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH010A_DISASTER/knjh010a_disasterindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjh010a_disasterindex.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    View::frame($args);
                    break 2;
                case "":
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH010A_DISASTER/knjh010a_disasterindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjh010a_disasterindex.php?cmd=edit";
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
$knjh010a_disasterCtl = new knjh010a_disasterController();
