<?php

require_once('for_php7.php');

require_once('knji060dModel.inc');
require_once('knji060dQuery.inc');

class knji060dController extends Controller
{
    public $ModelClassName = "knji060dModel";
    public $ProgramID      = "KNJI060D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":                                //メニュー画面もしくはSUBMITした場合
//                  $sessionInstance->knji060dModel();       コントロールマスタの呼び出し
                    $this->callView("knji060dForm1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXSEARCH4/knjxsearch4index.php?PROGRAMID=" .$this->ProgramID
                                        ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}"
                                        ."&TARGET=right_frame&PATH=" .urlencode("/I/KNJI060D/knji060dindex.php?cmd=edit");
                    $args["right_src"] = "knji060dindex.php?cmd=edit";
                    $args["cols"] = "50%,*";
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
$knji060dCtl = new knji060dController();
//var_dump($_REQUEST);
