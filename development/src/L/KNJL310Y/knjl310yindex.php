<?php

require_once('for_php7.php');

require_once('knjl310yModel.inc');
require_once('knjl310yQuery.inc');

class knjl310yController extends Controller
{
    public $ModelClassName = "knjl310yModel";
    public $ProgramID      = "KNJL310Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl310yForm1");
                    }
                    break 2;
                case "":
                case "knjl310y":
                case "change":
                    $sessionInstance->knjl310yModel();
                    $this->callView("knjl310yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl310yCtl = new knjl310yController();
