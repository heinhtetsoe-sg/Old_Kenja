<?php

require_once('for_php7.php');
require_once('knji120Model.inc');
require_once('knji120Query.inc');

class knji120Controller extends Controller
{
    public $ModelClassName = "knji120Model";
    public $ProgramID      = "KNJI120";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "chg_year":
                case "serch":
                case "clear":  //取消ボタン
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knji120Form2");
                    break 2;
                case "replace":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knji120SubForm1");
                    break 2;
                case "list":
                    $this->callView("knji120Form1");
                    break 2;
                case "replace_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getAddingModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
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
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/I/KNJI120/knji120index.php?cmd=edit") ."&button=2";
                    // no break
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP2/index.php" .$search;
                    $args["right_src"] = "knji120index.php?cmd=edit";
                    $args["cols"] = "25%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji120Ctl = new knji120Controller();
