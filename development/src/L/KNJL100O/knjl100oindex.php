<?php

require_once('for_php7.php');

require_once('knjl100oModel.inc');
require_once('knjl100oQuery.inc');

class knjl100oController extends Controller
{
    public $ModelClassName = "knjl100oModel";
    public $ProgramID      = "KNJL100O";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl100oForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl100oForm1");
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
$knjl100oCtl = new knjl100oController();
