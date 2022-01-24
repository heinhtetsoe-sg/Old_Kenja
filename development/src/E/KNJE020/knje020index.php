<?php

require_once('for_php7.php');

require_once('knje020Model.inc');
require_once('knje020Query.inc');

class knje020Controller extends Controller
{
    public $ModelClassName = "knje020Model";
    public $ProgramID      = "KNJE020";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "tsuchiTorikomi":
                case "select_pattern":
                    $this->callView("knje020Form1");
                    break 2;
                case "subform1": //成績参照
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje020SubForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje020Form1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "copy_pattern":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje020Form1");
                    $sessionInstance->getCopyPatternModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "list":
                    break 2;
                case "detail":
                    break 2;
                case "reset":
                    $this->callView("knje020Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE020/knje020index.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode($programpath."/knje020index.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    }
                    $args["right_src"] = "knje020index.php?cmd=edit&init=1";
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
$knje020Ctl = new knje020Controller();
//var_dump($_REQUEST);
