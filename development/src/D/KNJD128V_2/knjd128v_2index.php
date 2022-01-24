<?php

require_once('for_php7.php');


require_once('knjd128v_2Model.inc');
require_once('knjd128v_2Query.inc');

class knjd128v_2Controller extends Controller
{
    public $ModelClassName = "knjd128v_2Model";
    public $ProgramID      = "KNJD128V_2";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "slump_seq":
                case "reset":
                    $this->callView("knjd128v_2Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd128v_2Ctl = new knjd128v_2Controller();
