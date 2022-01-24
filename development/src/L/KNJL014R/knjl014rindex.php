<?php

require_once('for_php7.php');

require_once('knjl014rModel.inc');
require_once('knjl014rQuery.inc');

class knjl014rController extends Controller
{
    public $ModelClassName = "knjl014rModel";
    public $ProgramID      = "KNJL014R";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl014r":
                case "change":
                    $sessionInstance->knjl014rModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl014rForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl014rCtl = new knjl014rController();
