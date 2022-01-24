<?php

require_once('for_php7.php');

require_once('knja083Model.inc');
require_once('knja083Query.inc');

class knja083Controller extends Controller
{
    public $ModelClassName = "knja083Model";
    public $ProgramID      = "KNJA083";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja083":
                case "change":
                    $sessionInstance->knja083Model();       //コントロールマスタの呼び出し
                    $this->callView("knja083Form1");
                    exit;
                case "execute":
//                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecuteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knja083");
                    break 1;
                case "csv":     //CSVダウンロード
//                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja083Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja083Ctl = new knja083Controller();
