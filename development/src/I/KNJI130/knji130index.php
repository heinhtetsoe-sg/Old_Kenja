<?php

require_once('for_php7.php');

require_once('knji130Model.inc');
require_once('knji130Query.inc');

class knji130Controller extends Controller
{
    public $ModelClassName = "knji130Model";
    public $ProgramID      = "KNJI130";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knji130Form1");
                    break 2;
                case "right":
                    $this->callView("knji130Search");
                    break 2;
                case "edit":
                case "editFuku":
                case "clear":   //取消ボタン
                    $this->callView("knji130Form2");
                    break 2;
                case "fukugaku":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("editFuku");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knji130index.php?cmd=list";
                    $args["right_src"]  = "knji130index.php?cmd=right";
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
$knji130Ctl = new knji130Controller();
