<?php

require_once('for_php7.php');

require_once('knja113Model.inc');
require_once('knja113Query.inc');

class knja113Controller extends Controller
{
    public $ModelClassName = "knja113Model";
    public $ProgramID      = "KNJA113";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            $sessionInstance->knja113Model();        //コントロールマスタの呼び出し
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "change":
                    $this->callView("knja113Form1");
                    break 2;
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->schreg_chk($sessionInstance->field["SCHREGNO"]);
                    // no break
                case "edit":
                case "edit2":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knja113Form2");
                    break 2;
                case "add":
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "Ikkatsu":
                case "Ikkatsu2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knja113Ikkatsu");
                    break 2;
                case "ikkatsu_Insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getSubInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("Ikkatsu2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knja113index.php?cmd=list";
                    $args["right_src"] = "knja113index.php?cmd=edit";
                    $args["cols"] = "55%,45%";
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
$knja113Ctl = new knja113Controller;
