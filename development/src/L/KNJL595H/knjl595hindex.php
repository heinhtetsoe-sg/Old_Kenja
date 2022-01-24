<?php

require_once('for_php7.php');

require_once('knjl595hModel.inc');
require_once('knjl595hQuery.inc');

class knjl595hController extends Controller
{
    public $ModelClassName = "knjl595hModel";
    public $ProgramID      = "KNJL595H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl595hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl595hForm1");
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
$knjl595hCtl = new knjl595hController();
