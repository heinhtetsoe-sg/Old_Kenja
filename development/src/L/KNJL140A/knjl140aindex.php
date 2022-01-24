<?php

require_once('for_php7.php');

require_once('knjl140aModel.inc');
require_once('knjl140aQuery.inc');

class knjl140aController extends Controller
{
    public $ModelClassName = "knjl140aModel";
    public $ProgramID      = "KNJL140A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl140aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl140aCtl = new knjl140aController();
