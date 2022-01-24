<?php

require_once('for_php7.php');

require_once('knjl343qModel.inc');
require_once('knjl343qQuery.inc');

class knjl343qController extends Controller
{
    public $ModelClassName = "knjl343qModel";
    public $ProgramID      = "KNJL343Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl343qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl343qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl343qCtl = new knjl343qController();
