<?php

require_once('for_php7.php');
require_once('knji030Model.inc');
require_once('knji030Query.inc');

class knji030Controller extends Controller
{
    public $ModelClassName = "knji030Model";
    public $ProgramID      = "KNJI030";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knji030Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knji030Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/I/KNJI030/knji030index.php?cmd=edit")."&button=2";
                    $args["right_src"] = "knji030index.php?cmd=edit&schregno=''";
                    $args["cols"] = "23%,*";
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
$KNJI030Ctl = new knji030Controller();
//var_dump($_REQUEST);
